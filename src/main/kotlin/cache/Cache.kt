package cache

import helpers.logger
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

class Cache {
    companion object {
        lateinit var host: String
        lateinit var port: String
        lateinit var kredsClient: KredsClient

        fun connect(host: String, port: String) {
            this.host = host
            this.port = port

            kredsClient = newClient(Endpoint.from("${Companion.host}:${Companion.port}"))
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
