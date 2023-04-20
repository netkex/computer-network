import java.io.File

class Receiver {
    var finished = false
    var curInd = 0
    val data: MutableList<Byte> = mutableListOf()

    fun processMessage(message: List<Byte>): List<Byte> {
        if (message[0].toInt().toChar() == 'F') {
            finished = true
            writeToFile()
            return listOf()
        } else if (message[0].toInt().toChar() == 'M') {
            if (message.size < 7)
                return listOf('E'.code.toByte(), (1 - curInd).toByte())
            val ind = message[1].toInt()
            if (ind != curInd) {
                return listOf('A'.code.toByte(), (1 - curInd).toByte())
            }

            val checkSum = bytesToInt(message.subList(2, 6))
            val messageData = message.drop(6)
            val messageDataCheckSum = checkSum(messageData)

            if (checkSum(messageData) != checkSum) {
                println("Message was corrupted! Original check sum: $checkSum; data check sum: $messageDataCheckSum")
                return listOf('E'.code.toByte(), (1 - curInd).toByte())
            }

            curInd = 1 - curInd
            data.addAll(messageData)
            println("Current total size of received data: ${data.size}")
            return listOf('A'.code.toByte(), (1 - curInd).toByte())
        }

        return listOf('E'.code.toByte(), (1 - curInd).toByte())
    }

    fun writeToFile() {
        File("src/main/resources/received.txt").writeBytes(data.toByteArray())
    }

    fun Byte.toPositiveLong() = (toInt() and 0xFF).toLong()

    fun checkSum(message: List<Byte>): Long {
        var res = 0L
        for (i in message.indices step 4) {
            res = res xor bytesToInt(message.subList(i, Integer.min(i + 4, message.size)))
        }
        return res
    }

    fun bytesToInt(byteList: List<Byte>): Long {
        var step = 1L
        var res = 0L
        for (element in byteList) {
            res += element.toPositiveLong() * step
            step *= 256L
        }
        return res
    }
}