import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentSkipListSet


class Router(
    private val ip: String,
    private val inChannel: Channel<Message>,
    private val outChannel: Channel<Message>,
    private val timeout: Long) {
    private val distHistory = ConcurrentHashMap<String, Map<String, Int>>()
    private val lastUpdate = ConcurrentHashMap<String, Long>()

    @Volatile
    private var dist = mapOf<String, RouteInfo>()

    @Volatile
    private var edges = ConcurrentSkipListSet<String>()
    private val stopped = AtomicBoolean(false)

    data class RouteInfo(
        val hopsNum: Int,
        val nextHop: String
    )

    companion object {
        val logger = LoggerFactory.getLogger(Router::class.java)
        val DSTMAX = 15
    }

    fun stop() {
        logger.info("[Router ${this.ip}] Was stopped by user")
        stopped.set(true)
    }

    fun addEdge(ip: String) {
        edges.add(ip)
    }

    suspend fun sendUpdates() {
        while (true) {
            if (stopped.get())
                return

            delay(timeout)


            val distToSend = dist.mapValues { entry -> entry.value.hopsNum }.toMap()
            for (neighbourIp in edges) {
                outChannel.send(Message(
                    ip,
                    neighbourIp,
                    distToSend
                ))
            }

            val curTime = System.currentTimeMillis()
            val edgesToDelete = ArrayList<String>()
            for (ip in edges) {
                if (curTime - lastUpdate.getOrDefault(ip, curTime) > 3 * timeout) {
                    logger.info("[Router ${this.ip}] Remove edge to $ip due to timeout")
                    edgesToDelete.add(ip)
                }
            }

            edges.removeAll(edgesToDelete)
        }
    }

    suspend fun run() {

        runUpdate()
        with(CoroutineScope(coroutineContext)) {
            launch {
                sendUpdates()
            }
        }

        for (message in inChannel) {
            if (stopped.get()) {
                return
            }
            logger.info("[Router ${this.ip}] Update from neighbour: ${message.source}")

            lastUpdate[message.source] = System.currentTimeMillis()
            distHistory[message.source] = message.dist
            edges.add(message.source)
            runUpdate()
        }
    }

    private fun runUpdate()  {
        val newDist = calcDist()
        if (dist != newDist) {
            logDiff(dist, newDist)
            dist = newDist
        }
    }

    private fun calcDist(): HashMap<String, RouteInfo> {
        val newDist = HashMap<String, RouteInfo>()
        for (neighbour in edges) {
            newDist[neighbour] = RouteInfo(
                1,
                neighbour
            )

            distHistory[neighbour]?.forEach { (ip, dst) ->
                val hopNum = dst + 1
                if (hopNum <= DSTMAX) {
                    val curInfo = newDist[ip]
                    if (curInfo == null || curInfo.hopsNum > hopNum) {
                        newDist[ip] = RouteInfo(
                            hopNum,
                            neighbour
                        )
                    }
                }
            }
        }

        newDist.remove(ip)
        return newDist
    }

    private fun logDiff(dist: Map<String, RouteInfo>, newDist: Map<String, RouteInfo>) {
        for (entry in newDist) {
            val ip = entry.key
            val info = entry.value
            if (dist[ip] != info) {
                logger.info("[Router ${this.ip}] Path to ${ip}. Next hop: ${info.nextHop}. Number of hops: ${info.hopsNum}")
            }
        }
    }
}