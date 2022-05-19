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

        fun all(query: String = "*"): Map<String, Any> {
            return cacheTransaction {
                val result = HashMap<String, Any>()
                for (key in client.keys(query)) {
                    result[key] = client.get(key) ?: "what the fuck (this is internal server error, should never ever happen)"
                }
                return@cacheTransaction result
            }
        }
    }
}

class CacheTransaction(val client: KredsClient);

fun <T> cacheTransaction(block: suspend CacheTransaction.() -> T): T {
    return runBlocking {
        Cache.kredsClient.use { client ->
            with(CacheTransaction(client)) {
                return@runBlocking block()
            }
        }
    }
}
