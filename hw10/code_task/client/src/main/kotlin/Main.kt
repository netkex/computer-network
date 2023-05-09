import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required

fun main(args: Array<String>) {
    val parser = ArgParser("client")

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

    val file by parser.option(
        ArgType.String,
        "file",
        description = "file to write result"
    ).required()

    val windowSize by parser.option(
        ArgType.Int,
        "window_size",
        description = "size of window"
    ).required()

    val timeout by parser.option(
        ArgType.Int,
        "timeout"
    ).default(2000)

    val windowTimeout by parser.option(
        ArgType.Int,
        "window_timeout"
    ).default(2000)

    val fails by parser.option(
        ArgType.Int,
        "fails",
        description = "maximum number of timeout fails"
    ).default(5)

    val blockSize by parser.option(
        ArgType.Int,
        "block_size"
    ).default(4096)

    val failProb by parser.option(
        ArgType.Double,
        "fail probability"
    ).default(0.25)

    parser.parse(args)

    try {
        Client(
            host,
            port,
            timeout,
            file,
            blockSize,
            windowSize,
            windowTimeout,
            fails,
            failProb
        ).run()
    } catch (e: java.lang.RuntimeException) {
        println(e.message)
    }
}