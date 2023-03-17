import rawhttp.core.RawHttp
import rawhttp.core.body.BytesBody
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.Executors


class Server(port: Int,
             pathToStorage: String,
             private val bannedDomains: List<String>,
             private val bannedURI: List<String>) {
    private val socket = ServerSocket(port)
    private val executor = Executors.newSingleThreadExecutor()
    private val rawHttp = RawHttp()
    private val client = HttpClient.newHttpClient()

    private val badRequestResponse = rawHttp.parseResponse("400 Bad Request HTTP/1.1\r\nContent-Type: text/plain").eagerly()
    private val badGateWayResponse = rawHttp.parseResponse("502 Bad Gateway HTTP/1.1\r\nContent-Type: text/plain").eagerly()
    private val notFoundResponse   = rawHttp.parseResponse("404 Not Found HTTP/1.1\r\nContent-Type: text/plain").eagerly()
    private val forbiddenResponse  = rawHttp.parseResponse("403 Forbidden HTTP/1.1\r\nContent-Type: text/plain").eagerly();

    private val storage = ResponseStorage(pathToStorage)

    fun start() {
        println("start server on port ${socket.localPort}")
        while (true) {
            val newClient = socket.accept()
            executor.submit { handleClient(newClient) }
        }
    }

    private fun handleClient(newClient: Socket) {
        newClient.use { socket ->
            val request = rawHttp.parseRequest(socket.getInputStream())
            val uriStr = "https://" + request.uri.path.drop(1)
            val uri = URI.create(uriStr)

            if (bannedDomains.contains(uri.host) || bannedURI.contains(uriStr)) {
                forbiddenResponse.writeTo(socket.getOutputStream())
                return@use
            }

            when(request.method) {
                "GET" -> {
                    val response: Response
                    val proxyRequest = if (storage.checkURI(uriStr)) {
                        response = storage.getResponse(uriStr)
                        HttpRequest.newBuilder()
                            .uri(uri)
                            .GET()
                            .header("If-Modified-Since", response.lastModified)
                            .header("If-None-Match", response.etag)
                            .build()
                    } else {
                        response = Response("", "", "", ByteArray(0))
                        HttpRequest.newBuilder()
                            .uri(uri)
                            .GET()
                            .build()
                    }

                    try {
                        val proxyResponse = client.send(proxyRequest, HttpResponse.BodyHandlers.ofByteArray())
                        if (proxyResponse.statusCode() == 304) {
                            sendResponse(socket, response)
                        } else {
                            sendResponse(socket, proxyResponse, uriStr, true)
                        }
                    } catch (e: Exception) {
                        badGateWayResponse.writeTo(socket.getOutputStream())
                    }
                }
                "POST" -> {
                    val requestBody = request.body.map { bodyReader -> bodyReader.decodeBodyToString(Charsets.UTF_8) }.orElse("")

                    val proxyRequest = HttpRequest.newBuilder()
                        .uri(uri)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build()

                    try {
                        val response = client.send(proxyRequest, HttpResponse.BodyHandlers.ofByteArray())
                        sendResponse(socket, response)
                    } catch (e: Exception) {
                        badGateWayResponse.writeTo(socket.getOutputStream())
                    }
                }
                else -> {
                    badRequestResponse.writeTo(socket.getOutputStream())
                }
            }
        }
    }


    private fun sendResponse(socket: Socket, response: Response) {
        val okResponse = rawHttp.parseResponse("""
200 OK HTTP/1.1
Content-Type: ${response.contentType} 
Etag: ${response.etag}
Last-Modified: ${response.lastModified}
""").eagerly()

        okResponse.withBody(BytesBody(response.body)).writeTo(socket.getOutputStream())
    }

    private fun sendResponse(socket: Socket, httpResponse: HttpResponse<ByteArray>, uri: String = "", saveResponse: Boolean = false) {
        when (httpResponse.statusCode()) {
            200 -> {
                val response = extractResponse(httpResponse)
                if (saveResponse)
                    storage.saveResponse(uri, response)
                sendResponse(socket, response)
            }
            404 -> notFoundResponse.writeTo(socket.getOutputStream())
            else -> badGateWayResponse.writeTo(socket.getOutputStream())
        }
    }

    private fun extractResponse(response: HttpResponse<ByteArray>): Response {
        val headerMap = response.headers().map()
        return Response(
            contentType = headerMap.getOrDefault("Content-Type", listOf("text/plain"))[0],
            lastModified = headerMap.getOrDefault("Last-Modified", listOf(""))[0],
            etag = headerMap.getOrDefault("Etag", listOf(""))[0],
            body = response.body(),
        )
    }
}