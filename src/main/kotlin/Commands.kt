import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.CommandBuilder
import com.jessecorbett.diskord.bot.BotContext
import com.jessecorbett.diskord.bot.classicCommands
import com.jessecorbett.diskord.util.sendReply
import response.list
import response.new
import response.toEmbed

fun CommandBuilder.safeCommand(key: String, action: suspend BotContext.(Message) -> Unit) {
    command(key) { message ->
        try {
            action(message)
        } catch (e: Exception) {
            channel(message.channelId).sendReply(message, embed = Embed.new(null, "you are gay ($e)", null))
        }
    }
}

fun BotBase.bindCommands() {
    classicCommands("!") {
        safeCommand("ping") { message ->
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        safeCommand("new") { message ->
            new(message)
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        safeCommand("list") { message ->
            val responses = list(message)
            channel(message.channelId).sendReply(message, embed = responses.toEmbed())
        }
    }
}
