import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.classicCommands
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage
import response.ResponseTable
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import response.list
import response.new

suspend fun main() {
    val dotenv = Dotenv.load()

    Database.connect("jdbc:postgresql://localhost:5432/automod", "org.postgresql.Driver", "user", "password")
    transaction {
        SchemaUtils.create(ResponseTable)
    }

    bot(dotenv.get("TOKEN") ?: throw Exception("No token found")) {
        events {
            onReady {
                channel("806802320565993544").sendMessage("Hey there!")
            }
        }

        classicCommands("!") {
            command("ping") { message ->
                message.respond("no")
            }

            command("new") { message ->
                try {
                    new(message)
                    message.respond("no")
                }
                catch (e: Exception) {
                    message.respond("you are gay ($e)")
                }
            }

            command("list") { message ->
                try {
                    val responses = list(message)
                    message.respond(responses.joinToString("\n"))
                } catch (e: Exception) {
                    message.respond("you are gay ($e)")
                }
            }
        }
    }
}
