import kotlinx.coroutines.channels.Channel

class Broker(routers: List<String>) {
    private val inChannels = routers.associateWith { Channel<Message>(Channel.UNLIMITED) }
    private val outChannel = Channel<Message> (Channel.UNLIMITED)

    fun getInChannel(ip: String): Channel<Message> {
        return inChannels[ip] ?: throw java.lang.IllegalArgumentException("illegal ip: $ip")
    }

    fun getOutChannel(): Channel<Message> {
        return outChannel
    }

    suspend fun run() {
        for (message in outChannel) {
            inChannels[message.destination]?.send(message)
        }
    }
}