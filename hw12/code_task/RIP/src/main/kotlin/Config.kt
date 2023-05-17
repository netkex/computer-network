import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class Config(
    val concurrencyLevel: Int,
    val timeout: Long,
    val routers: List<String>,
    val edges: List<Edge>
) {
    @Suppress("unused")
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        @JvmStatic
        fun fromJson(json: String): Config {
            return Json.decodeFromString(json)
        }
    }
}

@kotlinx.serialization.Serializable
data class Edge(
    val u: String,
    val v: String
)