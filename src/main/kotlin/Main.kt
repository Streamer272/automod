import bot.bindEvents
import com.jessecorbett.diskord.bot.bot
import helpers.Cache
import helpers.cacheTransaction
import response.ResponseTable
import io.github.cdimascio.dotenv.Dotenv
import io.github.crackthecodeabhi.kreds.args.SyncOption
import joke.JokeTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import whitelist.WhitelistTable

suspend fun main() {
    val dotenv = Dotenv.load()
    val token = dotenv.get("TOKEN") ?: throw Exception("No token found")

    val db = dotenv.get("DB") ?: throw Exception("No database found")
    val dbHost = dotenv.get("DB_HOST") ?: throw Exception("No host found")
    val dbPort = dotenv.get("DB_PORT") ?: throw Exception("No port found")
    val dbUser = dotenv.get("DB_USER") ?: throw Exception("No database user found")
    val dbPassword = dotenv.get("DB_PASSWORD") ?: throw Exception("No database password found")

    val cacheHost = dotenv.get("CACHE_HOST") ?: throw Exception("No cache host found")
    val cachePort = dotenv.get("CACHE_PORT") ?: throw Exception("No cache port found")

    val enabledValues = dotenv.get("ENABLED") ?: "jokes, whitelist, cache"
    val enabledList = enabledValues.split(",").map { it.trim().lowercase() }
    val jokesEnabled = enabledList.contains("jokes")
    val whitelistEnabled = enabledList.contains("whitelist")
    val cacheEnabled = enabledList.contains("cache")

    Database.connect("jdbc:postgresql://$dbHost:$dbPort/$db", "org.postgresql.Driver", dbUser, dbPassword)
    Cache.connect("$cacheHost:$cachePort", cacheEnabled)

    transaction {
        SchemaUtils.create(ResponseTable)
        SchemaUtils.create(WhitelistTable)
        SchemaUtils.create(JokeTable)
    }

    cacheTransaction {
        client.flushAll(SyncOption.SYNC)
    }
    response.initCache()

    bot(token) {
        bindEvents(jokesEnabled, whitelistEnabled)
    }
}
