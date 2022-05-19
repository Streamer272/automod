package bot

import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.common.UserStatus
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.sendMessage

fun BotBase.bindEvents(responseEnabled: Boolean, whitelistEnabled: Boolean, jokesEnabled: Boolean) {
    lateinit var botId: String

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
                return@onMessageCreate executeCommand(message, this)
            }
            if (whitelistEnabled && whitelist.getWhitelist(message) != null) {
                return@onMessageCreate
            }

            if (responseEnabled) {
                val responses = response.respond(message)
                responses.map {
                    message.reply(it)
                }
                if (responses.isNotEmpty()) {
                    return@onMessageCreate
                }
            }

            if (jokesEnabled && joke.getRandomNumber()) {
                val jokeResponse = joke.respond(message) ?: return@onMessageCreate
                channel(message.channelId).sendMessage(jokeResponse.joke)
            }
        }
    }
}
