import kotlinx.cli.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.random.Random

fun send(socket: DatagramSocket, p: Double, packet: DatagramPacket) {
    if (Random.nextFloat() > p) {
        //simulate message corruption
        if (packet.length > 7 && Random.nextFloat() < p) {
            repeat(3) {
                packet.data[Random.nextInt(6, packet.length)] = Random.nextInt(0, 256).toByte()
            }
        }
        socket.send(packet)
    }
}

fun main(args: Array<String>) {
    val parser = ArgParser("server")
    val host by parser.option(ArgType.String, "host", "host", "server host").default("localhost")
    val port by parser.option(ArgType.Int, "port", "p", "server host").required()
    val file by parser.option(ArgType.String, "file", "fl", "file to transmit").required()
    val pl by parser.option(ArgType.Double, "probability", "pl", "lost packet probability").default(0.3)
    val timeOut by parser.option(ArgType.Int, "timeout", "t", "timeout in milliseconds").default(1000)
    val failTimes by parser.option(ArgType.Int, "fails", "f").default(10)

    parser.parse(args)

    val serverAddress = InetSocketAddress(host, port)
    val socket = DatagramSocket(InetSocketAddress(0))
    val ackPacket = DatagramPacket(ByteArray(4200), 4200)

    val sender = Sender(file, 4096)
    val firstMessage = sender.process(listOf())
    send(socket, pl, DatagramPacket(firstMessage.toByteArray(), firstMessage.size, serverAddress))

    var fail = 0
    socket.soTimeout = timeOut
    while (true) {
        val message = try {
            fail = 0
            socket.receive(ackPacket)
            val receivedMessage = List(ackPacket.length) { i -> ackPacket.data[i] }
            sender.process(receivedMessage)
        } catch (e: java.net.SocketTimeoutException) {
            fail++
            sender.process(listOf())
        }

        if (sender.finished)
            break
        if (fail > failTimes) {
            println("failed to send content")
            return
        }

        send(socket, pl, DatagramPacket(message.toByteArray(), message.size, serverAddress))
    }

    // closing connection
    send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'R'.code.toByte()).toByteArray(), 2, serverAddress))

    // wait for FA (Fin Ack)
    fail = 0
    while (true) {
        fail++
        try {
            socket.receive(ackPacket)
            val receivedMessage = List(ackPacket.length) { i -> ackPacket.data[i] }
            if (receivedMessage.size >= 2 && receivedMessage[0].toInt().toChar() == 'F' && receivedMessage[1].toInt().toChar() == 'A') {
                break
            }

            send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'R'.code.toByte()).toByteArray(), 2, serverAddress))
        } catch (_: java.net.SocketTimeoutException) { }

        if (fail > failTimes) {
            println("it is not guaranteed that content was sent because connection was not properly teared down")
            return
        }
    }

    // return FA
    fail = 0
    socket.soTimeout = 5 * timeOut
    while (true) {
        fail++
        try {
            socket.receive(ackPacket)
            val receivedMessage = List(ackPacket.length) { i -> ackPacket.data[i] }
            if (receivedMessage.size >= 2 && receivedMessage[0].toInt().toChar() == 'F' && receivedMessage[1].toInt().toChar() == 'R') {
                send(socket, pl, DatagramPacket(listOf('F'.code.toByte(), 'A'.code.toByte()).toByteArray(), 2, serverAddress))
            }
        } catch (_: java.net.SocketTimeoutException) {
            break
        }

        if (fail >= failTimes) {
            println("content was sent, but connection was not properly teared down")
        }
    }

    println("content was successfully sent")
}