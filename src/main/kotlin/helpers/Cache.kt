package helpers

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class Cache {
    companion object {
        private lateinit var url: String
        var cacheEnabled by Delegates.notNull<Boolean>()
        lateinit var kredsClient: KredsClient

        fun connect(url: String, cacheEnabled: Boolean) {
            this.url = url
            this.cacheEnabled = cacheEnabled

            kredsClient = newClient(Endpoint.from(url))
        }
    }
}

class CacheTransaction(val client: KredsClient);

fun KredsClient.all(query: String = "*"): Map<String, String>? {
    return cacheTransaction {
        val result = HashMap<String, String>()
        for (key in client.keys(query)) {
            result[key] = client.get(key) ?: "what the fuck (this is internal server error, should never ever happen)"
        }
        return@cacheTransaction result
    }
}

fun <T> cacheTransaction(block: suspend CacheTransaction.() -> T): T? {
    if (!Cache.cacheEnabled) {
        return null;
    }

    return runBlocking {
        Cache.kredsClient.use { client ->
            with(CacheTransaction(client)) {
                return@runBlocking block()
            }
        }
    }
}
