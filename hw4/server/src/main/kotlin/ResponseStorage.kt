import org.rocksdb.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class Response(
    val contentType: String,
    val lastModified: String,
    val etag: String,
    val body: ByteArray,
)

class ResponseStorage(storagePath: String) {
    private val logger: Logger = LoggerFactory.getLogger(ResponseStorage::class.java)

    private val writeOptions: WriteOptions
    private val db: TransactionDB
    private val handlers = HashMap<String, ColumnFamilyHandle>()

    init {
        RocksDB.loadLibrary()
        writeOptions = WriteOptions()
        writeOptions.setSync(true)

        val options = DBOptions()
            .setCreateIfMissing(true)
            .setCreateMissingColumnFamilies(true)

        val columnFamilyDescriptors = listOf(
            ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
            ColumnFamilyDescriptor("content_type".toByteArray()),
            ColumnFamilyDescriptor("last_modified".toByteArray()),
            ColumnFamilyDescriptor("etag".toByteArray()),
            ColumnFamilyDescriptor("body".toByteArray())
        )

        val localHandlers = mutableListOf<ColumnFamilyHandle>()
        db = TransactionDB.open(options, TransactionDBOptions(), storagePath, columnFamilyDescriptors, localHandlers)

        localHandlers.forEach { handler ->
            handlers[String(handler.name)] = handler
        }
    }

    fun saveResponse(uri: String, response: Response) {
        try {
            val transaction = db.beginTransaction(writeOptions)
            transaction.put(handlers["content_type"]!!, uri.toByteArray(), response.contentType.toByteArray())
            transaction.put(handlers["last_modified"]!!, uri.toByteArray(), response.lastModified.toByteArray())
            transaction.put(handlers["etag"]!!, uri.toByteArray(), response.etag.toByteArray())
            transaction.put(handlers["body"]!!, uri.toByteArray(), response.body)
            transaction.commit()
        } catch (e: RocksDBException) {
            logger.error("failed to save response ${e.message}")
            return
        }

        logger.info("stored response for {}", uri)
    }

    fun checkURI(uri: String): Boolean {
        return db.keyMayExist(handlers["body"]!!, uri.toByteArray(), null)
    }

    fun getResponse(uri: String): Response {
        return try {
            val res = db.multiGetAsList(
                listOf(handlers["content_type"]!!, handlers["last_modified"]!!, handlers["etag"]!!, handlers["body"]!!),
                List(4) { _ -> uri.toByteArray() }
            )
            Response(
                contentType = String(res[0]),
                lastModified = String(res[1]),
                etag = String(res[2]),
                body = res[3]
            )
        } catch (e: RocksDBException) {
            logger.error("failed to load response ${e.message}")
            Response("", "", "", byteArrayOf())
        }
    }
}