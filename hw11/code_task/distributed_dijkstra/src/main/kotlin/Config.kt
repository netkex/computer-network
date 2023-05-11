import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class Config(
    val N: Int,
    val concurrencyLevel: Int,
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
    val u: Int,
    val v: Int,
    val c: Int
)