import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.classicCommands
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage
import io.github.cdimascio.dotenv.Dotenv

suspend fun main(args: Array<String>) {
    val dotenv = Dotenv.load()

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
