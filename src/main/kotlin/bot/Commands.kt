package bot

import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import com.jessecorbett.diskord.api.common.*
import com.jessecorbett.diskord.util.authorId
import com.jessecorbett.diskord.util.sendReply
import com.jessecorbett.diskord.bot.BotContext
import helpers.NoEmbed
import helpers.YesEmbed
import helpers.customEmbed
import joke.toEmbed
import response.toEmbed
import whitelist.toEmbed

var helpEmbeds: List<EmbedField> = listOf()

val commands = listOf(
    // internal
    Command(listOf("p", "ping"), "Ping bot", needsAdmin = false, display = true) { message ->
        ez(message)
    },
    Command(listOf("", "h", "help"), "Display help", needsAdmin = true, display = false) { message ->
        channel(message.channelId).sendReply(
            message,
            embed = Embed.customEmbed(
                "hElP mE",
                "(you are retarded and cant write single fucking command)",
                helpEmbeds.toMutableList()
            )
        )
    },

    // Cleaning db
    Command(listOf("clean@response"), "Clean response table", needsAdmin = true, display = false) { message ->
        response.clean(message)
        ez(message)
    },
    Command(listOf("clean@whitelist"), "Clean whitelist table", needsAdmin = true, display = false) { message ->
        whitelist.clean(message)
        ez(message)
    },
    Command(listOf("clean"), "Clean database", needsAdmin = true, display = false) { message ->
        response.clean(message)
        whitelist.clean(message)
        ez(message)
    },

    // Response
    Command(listOf("n", "new"), "Create new response", needsAdmin = true, display = true) { message ->
        response.new(message)
        ez(message)
    },
    Command(listOf("l", "list"), "List responses", needsAdmin = true, display = true) { message ->
        val responses = response.list(message)
        channel(message.channelId).sendReply(message, embed = responses.toEmbed())
    },
    Command(listOf("d", "delete"), "Delete response", needsAdmin = true, display = true) { message ->
        response.delete(message)
        ez(message)
    },

    // Whitelist
    Command(listOf("w", "whitelist"), "Whitelist @somebody", needsAdmin = true, display = true) { message ->
        whitelist.add(message)
        ez(message)
    },
    Command(listOf("s", "show"), "Show whitelist", needsAdmin = true, display = true) { message ->
        val records = whitelist.show(message)
        val globalClient = global()
        channel(message.channelId).sendReply(
            message,
            embed = records.toEmbed(this) {
                val user = globalClient.getUser(it)
                "${user.username}#${user.discriminator}"
            }
        )
    },
    Command(
        listOf("b", "blacklist"),
        "Remove @somebody from whitelist",
        needsAdmin = true,
        display = true
    ) { message ->
        whitelist.remove(message)
        ez(message)
    },
    Command(listOf("w?", "whitelist?"), "Is @somebody whitelisted", needsAdmin = true, display = true) { message ->
        val whitelistRecord = whitelist.getWhitelist(message)
        ez(message, if (whitelistRecord != null) YesEmbed else NoEmbed)
    },

    // Joke
    Command(listOf("nj", "new@joke"), "Create new joke", needsAdmin = true, display = true) { message ->
        joke.new(message)
        ez(message)
    },
    Command(listOf("lj", "list@joke"), "List jokes", needsAdmin = true, display = true) { message ->
        val jokes = joke.list(message)
        channel(message.channelId).sendReply(message, embed = jokes.toEmbed())
    },
    Command(listOf("dj", "delete@joke"), "Delete joke", needsAdmin = true, display = true) { message ->
        joke.delete(message)
        ez(message)
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

suspend fun BotContext.ez(message: Message, embed: Embed = NoEmbed) {
    channel(message.channelId).sendReply(message, embed = embed)
}
