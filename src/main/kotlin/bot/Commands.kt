package bot

import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.common.*
import com.jessecorbett.diskord.util.authorId
import com.jessecorbett.diskord.util.sendReply
import response.*
import com.jessecorbett.diskord.bot.BotContext

var helpEmbeds: List<EmbedField>? = null

val commands = listOf(
    Command(listOf("p", "ping"), "Ping bot", needsAdmin = false, display = true) { message ->
        channel(message.channelId).sendReply(message, embed = NoEmbed)
    },
    Command(listOf("n", "new"), "Create new response", needsAdmin = true, display = true) { message ->
        new(message)
        channel(message.channelId).sendReply(message, embed = NoEmbed)
    },
    Command(listOf("l", "list"), "List all responses", needsAdmin = true, display = true) { message ->
        val responses = list(message)
        channel(message.channelId).sendReply(message, embed = responses.toEmbed())
    },
    Command(listOf("d", "delete"), "Delete response", needsAdmin = true, display = true) { message ->
        delete(message)
        channel(message.channelId).sendReply(message, embed = NoEmbed)
    },
    Command(listOf("clean"), "Clean database", needsAdmin = true, display = false) { message ->
        clean(message)
        channel(message.channelId).sendReply(message, embed = NoEmbed)
    },
    Command(listOf("", "h", "help"), "Display help", needsAdmin = true, display = false) { message ->
        channel(message.channelId).sendReply(
            message,
            embed = Embed.customEmbed(
                "hElP mE",
                "(you are retarded and cant write single fucking command)",
                helpEmbeds!!.toMutableList()
            )
        )
    }
)

class SusException(message: String) : Exception(message)

suspend fun executeCommand(message: Message, context: BotContext) {
    val command = commands.findBy(message.content) ?: return commands.find { it.aliases.contains("help") }!!
        .block(context, message)
    if (command.needsAdmin) assertAdmin(message, context)

    with(context) {
        try {
            command.block(context, message)
        } catch (e: Exception) {
            val description = when (e) {
                is SusException -> e.message
                else -> "you are gay (${e.message})"
            }
            if (e !is SusException) e.printStackTrace()
            channel(message.channelId).sendReply(message, embed = Embed.customEmbed(null, description, null))
        }
    }
}

suspend fun assertAdmin(message: Message, context: BotContext) {
    with(context) {
        // bot creator: Streamer272#1523
        if (message.author.id == "619963897872646168") return@assertAdmin

        val author = message.guild!!.getMember(message.authorId)
        val roles = message.guild!!.getRoles()
            .filter { it.permissions.contains(Permission.ADMINISTRATOR) }
            .map { it.id }
        val isAdmin = author.roleIds.any { roles.contains(it) }
        if (!isAdmin) throw SusException("fuck you")
    }
}
