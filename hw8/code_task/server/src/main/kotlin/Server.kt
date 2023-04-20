import kotlinx.cli.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.random.Random

fun send(socket: DatagramSocket, p: Double, packet: DatagramPacket) {
    if (Random.nextFloat() > p) {
        socket.send(packet)
    }
}

fun main(args: Array<String>) {
    val parser = ArgParser("server")
    val port by parser.option(ArgType.Int, "port", "p").required()
    val pl by parser.option(ArgType.Double, "probability", "pl", "lost packet probability").default(0.3)
    val timeOut by parser.option(ArgType.Int, "timeout", "t", "timeout in milliseconds").default(1000)
    val failTimes by parser.option(ArgType.Int, "fails", "f").default(10)

    parser.parse(args)
    val socket = DatagramSocket(InetSocketAddress(port))
    val requestPacket = DatagramPacket(ByteArray(4200), 4200)
    socket.receive(requestPacket)

    var fail = 0
    socket.soTimeout = timeOut

    val receiver = Receiver()
    while (true) {
        try {
            fail = 0
            socket.receive(requestPacket)
            val receivedMessage = List(requestPacket.length) { i -> requestPacket.data[i] }
            val outputMessage = receiver.processMessage(receivedMessage)

            if (receiver.finished)
                break
            else
                send(socket, pl, DatagramPacket(outputMessage.toByteArray(), outputMessage.size, requestPacket.socketAddress))
        } catch (e: java.net.SocketTimeoutException) {
            fail++
            if (fail >= failTimes) {
                println("failed to receive content")
                return
            }
        }
    }

    // closing connection
    send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'A'.code.toByte()).toByteArray(), 2, requestPacket.socketAddress))
    fail = 0
    while (true) {
        fail++
        try {
            socket.receive(requestPacket)
            val receivedMessage = List(requestPacket.length) { i -> requestPacket.data[i] }
            if (receivedMessage.size >= 2 && receivedMessage[0].toInt().toChar() == 'F' && receivedMessage[1].toInt().toChar() == 'A') {
                break
            }

            send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'A'.code.toByte()).toByteArray(), 2, requestPacket.socketAddress))
            send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'R'.code.toByte()).toByteArray(), 2, requestPacket.socketAddress))
        } catch (_: java.net.SocketTimeoutException) {
            send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'R'.code.toByte()).toByteArray(), 2, requestPacket.socketAddress))
        }

        if (fail >= failTimes) {
            println("content was received, but connection was not properly teared down")
            return
        }
    }

    println("content was successfully received")
}