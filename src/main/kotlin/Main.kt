import bot.bindEvents
import cache.Cache
import cache.cacheTransaction
import com.jessecorbett.diskord.bot.bot
import response.ResponseTable
import io.github.cdimascio.dotenv.Dotenv
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

    Database.connect("jdbc:postgresql://$dbHost:$dbPort/$db", "org.postgresql.Driver", dbUser, dbPassword)
    Cache.connect(cacheHost, cachePort)

    transaction {
        SchemaUtils.create(ResponseTable)
        SchemaUtils.create(WhitelistTable)
        SchemaUtils.create(JokeTable)
    }

    bot(token) {
        bindEvents()
    }
}
