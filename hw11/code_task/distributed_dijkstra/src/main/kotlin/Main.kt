import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Scanner
import java.util.concurrent.Executors

fun main(args: Array<String>) = runBlocking {
    val parser = ArgParser("distributed dijkstra")

    val configPath by parser.option(
        ArgType.String,
        "config",
        "c",
        "path to config"
    ).required()

    parser.parse(args)

    val config = Config.fromJson(File(configPath).readText())

    val broker = Broker(config.N)
    val nodes = List(config.N) { id -> Node(id, broker.getInChannel(id), broker.getOutChannel(), config.N) }
    config.edges.forEach { edge ->
        nodes[edge.v].addOrChangeEdge(edge.u, edge.c)
        nodes[edge.u].addOrChangeEdge(edge.v, edge.c)
    }

    val dispatcher = Executors
        .newFixedThreadPool(config.concurrencyLevel)
        .asCoroutineDispatcher()

    launch(dispatcher) {
        broker.run()
    }

    nodes.forEach { node ->
        launch(dispatcher) { node.run() }
    }

    with (Scanner(System.`in`)) {
        while (true) {
            val u = nextInt()
            val v = nextInt()
            val c = nextInt()

            if (u < 0 || u >= config.N || v < 0 || v >= config.N) {
                println("incorrect nodes")
                continue
            }
            if (c <= 0) {
                println("incorrect cost")
                continue
            }

            nodes[u].addOrChangeEdge(v, c)
            nodes[v].addOrChangeEdge(u, c)
        }
    }
}