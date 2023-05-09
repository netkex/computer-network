import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required

fun main(args: Array<String>) {
    val parser = ArgParser("server")

    val host by parser.option(
        ArgType.String,
        "host",
        description = "server host name"
    ).required()

    val port by parser.option(
        ArgType.Int,
        "port",
        description = "server port num"
    ).required()

    val timeout by parser.option(
        ArgType.Int,
        "timeout"
    ).default(2000)

    val fails by parser.option(
        ArgType.Int,
        "fails",
        description = "maximum number of timeout fails"
    ).default(5)

    val file by parser.option(
        ArgType.String,
        "file",
        description = "file to write result"
    ).default("src/main/resources/received.txt")

    val failProb by parser.option(
        ArgType.Double,
        "fail probability"
    ).default(0.25)

    parser.parse(args)

    try {
        Server(
            host,
            port,
            timeout,
            fails,
            file,
            failProb
        ).run()
    } catch (e: java.lang.RuntimeException) {
        println(e.message)
    }
}