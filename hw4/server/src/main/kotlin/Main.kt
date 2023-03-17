import kotlinx.cli.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

fun loadConfig(configPath: String): Config {
    val configFileContent = File(configPath).readText()
    return Json.decodeFromString(configFileContent)
}

fun main(args: Array<String>) {
    val parser = ArgParser("server")
    val configFile by parser.option(
        ArgType.String,
        "config_file",
        "c"
    ).required()
    parser.parse(args)

    val config = try {
        loadConfig(configFile)
    } catch (e: Exception) {
        println("failed to parse config: ${e.message}")
        return
    }

    try {
        Server(config.port, config.storagePath, config.BannedDomains, config.BannedURL).start()
    } catch (e: Exception) {
        println("failed to run server: ${e.message}")
    }
}