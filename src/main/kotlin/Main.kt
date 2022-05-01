import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.classicCommands
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage
import entities.ResponseTable
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun main(args: Array<String>) {
    val dotenv = Dotenv.load()

    Database.connect("jdbc:postgresql://localhost:5432/automod", "org.postgresql.Driver", "user", "password")
    transaction {
        SchemaUtils.create(ResponseTable)
    }

    bot(dotenv.get("TOKEN") ?: throw Exception("No token found")) {
        events {
            onReady {
                println("Ready!")
                channel("806802320565993544").sendMessage("Hey there!")
            }
        }

        classicCommands("!") {
            command("ping") {
                it.respond("Pong!")
            }
        }
    }
}
