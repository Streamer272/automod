import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage
import com.jessecorbett.diskord.util.sendReply
import response.respond

fun BotBase.bindEvents() {
    var botId: String? = null

    events {
        onReady {
            channel("806802320565993544").sendMessage("Hey there!")
            botId = it.user.id
        }

        onMessageCreate { message ->
            if (message.author.id == botId) {
                return@onMessageCreate
            }

            val response = respond(message)
            response.map {
                channel(message.channelId).sendReply(message, it.response)
            }
        }
    }
}
