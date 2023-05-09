import kotlinx.coroutines.channels.Channel
import java.lang.Integer.min
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory


class Node(
    private val id: Int,
    val inChannel: Channel<Message>,
    private val outChannel: Channel<Message>,
    private val N: Int) {

    private val distHistory = HashMap<Int, List<Int>>()
    private var dist = List(N) { -1 }
    private var edges = ConcurrentHashMap<Int, Int>()
    private val running = AtomicBoolean(false)

    companion object {
        val logger = LoggerFactory.getLogger(Node::class.java)
    }

    suspend fun addOrChangeEdge(id: Int, cost: Int) {
        edges[id] = cost
        if (running.get()) {
            logger.info("Node ${this.id}. User update, update edge to $id by $cost cost")
            runUpdate()
        }
    }

    suspend fun run() {
        running.set(true)
        runUpdate()

        for (message in inChannel) {
            if (!distHistory.containsKey(message.source) || distHistory[message.source] != message.dist) {
                logger.info("Node $id. Update from neighbour: ${message.source}")
                distHistory[message.source] = message.dist
                runUpdate()
            }
        }
        println("lel")
    }

    private suspend fun sendUpdate() {
        for ((v, _) in edges) {
            outChannel.send(Message(id, v, dist))
        }
    }

    private suspend fun runUpdate()  {
        val newDist = calcDist()
        if (newDist != dist) {
            dist = newDist
            logStatus()
            sendUpdate()
        }
    }

    private fun calcDist(): List<Int> {
        val newDist = MutableList(N) { Int.MAX_VALUE }
        newDist[id] = 0
        for ((v, c) in edges) {
            distHistory[v]?.forEachIndexed { id, d -> if (d != -1) newDist[id] = min(newDist[id], c + d) }
            newDist[v] = min(newDist[v], c)
        }
        return newDist.map { d -> if (d == Int.MAX_VALUE) -1 else d }
    }

    private fun logStatus() {
        val distStr = dist.mapIndexed { id, d -> "$id: $d"}.joinToString(" | ")
        logger.info("Node $id. Updated distance to others: $distStr")
    }
}