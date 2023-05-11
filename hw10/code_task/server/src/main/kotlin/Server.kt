import java.io.File
import java.net.*
import kotlin.random.Random

class Server(
    host: String,
    port: Int,
    timeout: Int,
    private val failLimit: Int,
    private val file: String,
    private val failProbability: Double) {

    private val socket = DatagramSocket(InetSocketAddress(host, port))
    private val data: MutableList<Byte> = mutableListOf()
    private val datagramPacket = DatagramPacket(ByteArray(64000), 64000)

    init {
        socket.receive(datagramPacket)
        socket.soTimeout = timeout
    }

    fun run() {
        var fails = 0
        var nextSeqNum = 0
        while (true) {
            try {
                socket.receive(datagramPacket)
                fails = 0

                val message = Message.fromDatagram(datagramPacket)
                if (message.type == MessageType.FINREQ) {
                    shutdown()
                    return
                }

                if (message.num <= nextSeqNum)
                    if (message.num == nextSeqNum) {
                        nextSeqNum += 1
                        data.addAll(message.data.toList())
                        println("Current total size of received data: ${data.size}; Next seq num: $nextSeqNum")
                    }
                    send(Message(MessageType.ACK, message.num).toDatagram(datagramPacket.socketAddress))
                    continue
            } catch (_: SocketTimeoutException) {
                fails++
                if (fails > failLimit) {
                    println("failed to receive content")
                    throw java.lang.RuntimeException("too many receive timeouts")
                }
            }
        }
    }

    private fun shutdown() {
        writeToFile()
        sendFin()

        var fails = 0
        while (true) {
            try {
                socket.receive(datagramPacket)
                fails = 0
                val message = Message.fromDatagram(datagramPacket)
                if (message.type == MessageType.FINACK) {
                    println("content was successfully received")
                    return
                } else {
                    sendFin()
                }
            } catch (_: SocketTimeoutException) {
                fails++
                if (fails > failLimit) {
                    println("content was received, but finish was not graceful")
                    return
                }
            }
        }
    }

    private fun sendFin() {
        send(Message(MessageType.FINACK).toDatagram(datagramPacket.socketAddress))
        send(Message(MessageType.FINREQ).toDatagram(datagramPacket.socketAddress))
    }

    private fun send(datagramPacket: DatagramPacket) {
        if (Random.nextFloat() < failProbability)
            return
        socket.send(datagramPacket)
    }

    private fun writeToFile() {
        File(file).writeBytes(data.toByteArray())
    }
}