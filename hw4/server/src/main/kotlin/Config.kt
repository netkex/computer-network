import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Config @OptIn(ExperimentalSerializationApi::class) constructor(
    @JsonNames("port") val port: Int,
    @JsonNames("storage_path") val storagePath: String,
    @JsonNames("banned_domains") val BannedDomains: List<String> = listOf(),
    @JsonNames("banned_urls") val BannedURL: List<String> = listOf(),
)