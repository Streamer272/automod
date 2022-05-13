import bot.bindEvents
import com.jessecorbett.diskord.bot.bot
import response.ResponseTable
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import whitelist.WhitelistTable

suspend fun main() {
    val dotenv = Dotenv.load()
    val token = dotenv.get("TOKEN") ?: throw Exception("No token found")

    Database.connect("jdbc:postgresql://localhost:5432/automod", "org.postgresql.Driver", "user", "password")
    transaction {
        SchemaUtils.create(ResponseTable)
        SchemaUtils.create(WhitelistTable)
    }

    bot(token) {
        bindEvents()
    }
}
