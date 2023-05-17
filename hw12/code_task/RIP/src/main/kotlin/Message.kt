data class Message(
    val source: String,
    val destination: String,
    val dist: Map<String, Int>
)