import java.io.File

class Sender(filePath: String, val packetSize: Int = 4096) {
    var finished = false
    var curInd = 0
    var it = 0
    val data: List<Byte> = File(filePath).readBytes().toList()

    fun process(message: List<Byte>): List<Byte> {
        if (message.size < 2) {
            return listOf('M'.code.toByte(), curInd.toByte()) + getBlock()
        } else {
            if (message[0].toInt().toChar() == 'A' && message[1].toInt() == curInd) {
                curInd = 1 - curInd
                it += packetSize
                println("new byte iterator value: $it")
            }
            val block = getBlock()
            if (block.isEmpty()) {
                finished = true
                return listOf()
            }

            val checkSum = checkSum(block)
            return listOf('M'.code.toByte(), curInd.toByte()) + intToBytes(checkSum) + block
        }
    }

    fun getBlock(): List<Byte> {
        if (it >= data.size)
            return listOf()
        return data.subList(it, if (it + packetSize <= data.size) it + packetSize else data.size)
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

    fun intToBytes(x: Long): List<Byte> {
        val res = mutableListOf<Byte>()
        var xc = x
        repeat(4) {
            res.add((xc % 256L).toByte())
            xc /= 256L
        }

        return res
    }
}