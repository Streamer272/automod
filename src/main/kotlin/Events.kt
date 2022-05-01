import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendReply
import response.respond

fun BotBase.bindEvents() {
    var botId: String? = null

    events {
        onReady {
            botId = it.user.id
        }

        onMessageCreate { message ->
            if (message.author.id == botId || message.content.startsWith("!") || message.content.startsWith("\\")) {
                return@onMessageCreate
            }

            val response = respond(message)
            response.map {
                channel(message.channelId).sendReply(message, it.response)
            }
        }
    }
}
