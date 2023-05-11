import kotlinx.coroutines.channels.Channel

class Broker(N: Int) {
    private val inChannels = List<Channel<Message>>(N) { Channel(Channel.UNLIMITED) }
    private val outChannel = Channel<Message> (Channel.UNLIMITED)

    fun getInChannel(id: Int): Channel<Message> {
        return inChannels[id]
    }

    fun getOutChannel(): Channel<Message> {
        return outChannel
    }

    suspend fun run() {
        for (message in outChannel) {
            val channel = inChannels[message.destination]
            channel.send(message)
        }
    }
}