package helpers

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.runBlocking

class Cache {
    companion object {
        private lateinit var url: String
        lateinit var kredsClient: KredsClient

        fun connect(url: String) {
            this.url = url

            kredsClient = newClient(Endpoint.from(url))
        }
    }
}

class CacheTransaction(val client: KredsClient);

fun cacheTransaction(block: suspend CacheTransaction.() -> Unit) {
    try {
        runBlocking {
            Cache.kredsClient.use { client ->
                with(CacheTransaction(client)) {
                    block()
                }
            }
        }
    } catch (e: Exception) {
        logger.error { "Cache error: $e" }
    }
}
