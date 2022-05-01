import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.CommandBuilder
import com.jessecorbett.diskord.bot.BotContext
import com.jessecorbett.diskord.bot.classicCommands
import response.list
import response.new

fun CommandBuilder.safeCommand(key: String, action: suspend BotContext.(Message) -> Unit) {
    command(key) { message ->
        try {
            action(message)
        } catch (e: Exception) {
            message.respond("you are gay ($e)")
        }
    }
}

fun BotBase.bindCommands() {
    classicCommands("!") {
        safeCommand("ping") { message ->
            message.respond("no")
        }

        safeCommand("new") { message ->
            new(message)
            message.respond("no")
        }

        safeCommand("list") { message ->
            val responses = list(message)
            message.respond(responses.joinToString("\n"))
        }
    }
}
