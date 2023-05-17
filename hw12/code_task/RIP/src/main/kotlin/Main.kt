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
    val parser = ArgParser("rip")

    val configPath by parser.option(
        ArgType.String,
        "config",
        "c",
        "path to config"
    ).required()

    parser.parse(args)

    val config = Config.fromJson(File(configPath).readText())

    val broker = Broker(config.routers)
    val nodes = config.routers.associateWith { ip ->
        Router(ip, broker.getInChannel(ip), broker.getOutChannel(), config.timeout)
    }

    config.edges.forEach { edge ->
        nodes[edge.v]?.addEdge(edge.u) ?: throw java.lang.RuntimeException("invalid edge in config $edge")
        nodes[edge.u]?.addEdge(edge.v) ?: throw java.lang.RuntimeException("invalid edge in config $edge")
    }

    val dispatcher = Executors
        .newFixedThreadPool(config.concurrencyLevel)
        .asCoroutineDispatcher()

    launch(dispatcher) {
        broker.run()
    }

    nodes.forEach { (_, node) ->
        launch(dispatcher) { node.run() }
    }

    with (Scanner(System.`in`)) {
        while (true) {
            val command = next()
            val ip = next()

            if (command == "stop") {
                nodes[ip]?.stop() ?: println("incorrect ip $ip")
            } else {
                println("incorrect command")
            }
        }
    }
}

