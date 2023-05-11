import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.SocketAddress

enum class MessageType {
    DATA,
    ACK,
    FINREQ,
    FINACK
}

@kotlinx.serialization.Serializable
data class Message(
    val type: MessageType,
    val num: Int = -1,
    val data: ByteArray = ByteArray(0)
) {
    override fun toString(): String {
        return super.toString()
    }

    fun toDatagram(address: SocketAddress): DatagramPacket {
        val message = Json.encodeToString(this).toByteArray()
        return DatagramPacket(message, message.size, address)
    }

    companion object {
        @JvmStatic
        fun fromDatagram(datagramPacket: DatagramPacket): Message {
            val message = datagramPacket.data.copyOfRange(0, datagramPacket.length).toString(Charsets.UTF_8)
            return Json.decodeFromString(message)
        }
    }
}
