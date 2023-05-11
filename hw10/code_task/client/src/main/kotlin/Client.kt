import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import java.io.File
import java.lang.Integer.min
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.random.Random

class Client(
    host: String,
    port: Int,
    timeout: Int,
    file: String,
    blockSize: Int,
    private val windowSize: Int,
    private val windowTimeout: Int,
    private val failLimit: Int,
    private val failProbability: Double) {

    private val socket = DatagramSocket(InetSocketAddress(0))
    private val data: List<ByteArray>
    private val serverAddress = InetSocketAddress(host, port)

    private val datagramPacket = DatagramPacket(ByteArray(64000), 64000)
    private val terminal = Terminal()

    init {
        socket.soTimeout = timeout
        val dataBuilder = mutableListOf<ByteArray>()
        val fileData = File(file).readBytes()
        var it = 0
        while (it < fileData.size) {
            val nextIt = if (it + blockSize < fileData.size) it + blockSize else fileData.size
            dataBuilder.add(fileData.copyOfRange(it, nextIt))
            it += blockSize
        }

        data = dataBuilder
        println(data.size)
    }

    fun run() {
        sendWindow(0)
        var curSeq = 0
        var fails = 0
        var timerBegin = System.currentTimeMillis()

        while (true) {
            if (System.currentTimeMillis() - timerBegin > windowTimeout) {
                sendWindow(curSeq)
                timerBegin = System.currentTimeMillis()
            }

            try {
                socket.receive(datagramPacket)

                fails = 0
                val message = Message.fromDatagram(datagramPacket)
                if (message.type == MessageType.ACK && message.num == curSeq) {
                    timerBegin = System.currentTimeMillis()
                    curSeq++

                    if (curSeq == data.size) {
                        shutdown()
                        return;
                    }

                    printProgressBar(curSeq)
                    if (curSeq + windowSize < data.size)
                        send(Message(MessageType.DATA, curSeq + windowSize, data[curSeq + windowSize]).toDatagram(serverAddress))
                }
            } catch (_: SocketTimeoutException) {
                fails++
                if (fails > failLimit) {
                    println("failed to send content")
                    throw java.lang.RuntimeException("too many receive timeouts")
                }
            }
        }
    }

    private fun shutdown() {
        var fails = 0
        while (true) {
            send(Message(MessageType.FINREQ).toDatagram(serverAddress))
            try {
                socket.receive(datagramPacket)
                val message = Message.fromDatagram(datagramPacket)
                if (message.type == MessageType.FINACK)
                    break
            } catch (_: SocketTimeoutException) {
                fails++
                if (fails > failLimit) {
                    println("failed to send content")
                    throw java.lang.RuntimeException("too many receive timeouts")
                }
            }
        }

        fails = 0
        send(Message(MessageType.FINACK).toDatagram(serverAddress))
        socket.soTimeout = socket.soTimeout * 3
        while (true) {
            try {
                socket.receive(datagramPacket)
                fails++
                if (fails > failLimit) {
                    println("data send successfully, but shutdown was not graceful")
                    return
                }
                send(Message(MessageType.FINACK).toDatagram(serverAddress))
            } catch (_: SocketTimeoutException) {
                println("data send successfully")
                return
            }
        }
    }

    private fun sendWindow(windowBegin: Int) {
        data.subList(windowBegin, min(data.size, windowBegin + windowSize)).forEachIndexed { id, block ->
            send(Message(MessageType.DATA, windowBegin + id, block).toDatagram(serverAddress))
        }
    }

    private fun send(datagramPacket: DatagramPacket) {
        if (Random.nextFloat() < failProbability)
            return
        socket.send(datagramPacket)
    }

    fun printProgressBar(curSeq: Int) {
        val sended = (0 until curSeq).joinToString(" ")
        val current = (curSeq + 1 until min(data.size, curSeq + windowSize)).joinToString(" ")
        val waiting = (curSeq + windowSize + 1 until data.size).joinToString(" ")

        terminal.println("$sended ${TextColors.blue("[")} ${TextColors.red(curSeq.toString())} ${TextColors.green(current)} ${TextColors.blue("]")} ${TextColors.green(waiting)}")
    }
}