package bot

import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.common.*
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.CommandBuilder
import com.jessecorbett.diskord.bot.classicCommands
import com.jessecorbett.diskord.util.authorId
import com.jessecorbett.diskord.util.sendReply
import response.*
import com.jessecorbett.diskord.bot.BotContext

class SussyException(message: String) : Exception(message)

fun CommandBuilder.customCommand(vararg key: String, block: suspend BotContext.(Message) -> Unit) {
    key.map {
        command(it) { message ->
            try {
                block(message)
            } catch (e: Exception) {
                val description = when (e) {
                    is SussyException -> e.message
                    else -> "you are gay (${e.message})"
                }
                if (e !is SussyException) e.printStackTrace()
                channel(message.channelId).sendReply(message, embed = Embed.customEmbed(null, description, null))
            }
        }
    }
}

fun BotBase.bindCommands() {
    classicCommands("!") {
        customCommand("p", "ping") { message ->
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        customCommand("n", "new") { message ->
            assertAdmin(message, this)

            new(message)
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        customCommand("l", "list") { message ->
            assertAdmin(message, this)

            val responses = list(message)
            channel(message.channelId).sendReply(message, embed = responses.toEmbed())
        }

        customCommand("d", "delete") { message ->
            assertAdmin(message, this)

            delete(message)
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        customCommand("clean") { message ->
            assertAdmin(message, this)

            clean(message)
            channel(message.channelId).sendReply(message, embed = NoEmbed)
        }

        customCommand("") { message ->
            assertAdmin(message, this)

            val commands: MutableList<EmbedField> = mutableListOf()

            commands.add(EmbedField("p | ping", "Ping bot", false))
            commands.add(EmbedField("n | new", "Create a new response", false))
            commands.add(EmbedField("l | list", "List all responses", false))
            commands.add(EmbedField("d | delete", "Delete a response", false))

            channel(message.channelId).sendReply(message, embed = Embed.customEmbed("hElP mE", "(you are retarded and cant write single fucking command)", commands))
        }
    }
}

suspend fun assertAdmin(message: Message, context: BotContext) {
    with(context) {
        val author = message.guild!!.getMember(message.authorId)
        val roles = message.guild!!.getRoles().filter { it.permissions.contains(com.jessecorbett.diskord.api.common.Permission.ADMINISTRATOR) }.map { it.id }
        val isAdmin = author.roleIds.any { roles.contains(it) }
        if (!isAdmin) throw SussyException("fuck you")
    }
}
