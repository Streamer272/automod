package bot

import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.common.UserStatus
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendReply
import response.*

fun BotBase.bindEvents() {
    var botId: String? = null

    events {
        onReady {
            botId = it.user.id
            setStatus("Fucking your mom", UserStatus.DO_NOT_DISTURB)

            helpEmbeds = commands.filter { command ->
                command.display
            }.map { command ->
                EmbedField(command.aliases.joinToString(" | "), command.help, false)
            }
        }

        onMessageCreate { message ->
            if (message.author.id == botId || message.content.startsWith("\\")) {
                return@onMessageCreate
            }
            if (message.content.startsWith("!")) {
                executeCommand(message, this)
            }

            val response = respond(message)
            response.map {
                channel(message.channelId).sendReply(message, it.response)
            }
        }
    }
}
