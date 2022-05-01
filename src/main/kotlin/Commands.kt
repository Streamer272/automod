import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.CommandBuilder
import com.jessecorbett.diskord.bot.BotContext
import com.jessecorbett.diskord.bot.classicCommands
import com.jessecorbett.diskord.util.sendReply
import response.*

fun CommandBuilder.safeCommand(key: String, block: suspend BotContext.(Message) -> Unit) {
    command(key) { message ->
        try {
            block(message)
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

        safeCommand("delete") { message ->
            delete(message)
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        safeCommand("clean") { message ->
            clean(message)
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        safeCommand("") { message ->
            val commands: MutableList<EmbedField> = mutableListOf()

            commands.add(EmbedField("!ping", "Ping bot", false))
            commands.add(EmbedField("!new", "Create a new response", false))
            commands.add(EmbedField("!list", "List all responses", false))
            commands.add(EmbedField("!delete", "Delete a response", false))

            channel(message.channelId).sendReply(message, embed = Embed.new("hElP mE", "(you are retarded and cant write single fucking command)", commands))
        }
    }
}
