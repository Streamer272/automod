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

fun CommandBuilder.safeCommand(key: String, block: suspend BotContext.(Message) -> Unit) {
    command(key) { message ->
        try {
            block(message)
        } catch (e: Exception) {
            val description = when (e) {
                is SussyException -> e.message
                else -> "you are gay (${e.message})"
            }
            if (e !is SussyException) e.printStackTrace()
            channel(message.channelId).sendReply(message, embed = Embed.new(null, description, null))
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
            assertAdmin(message, this)

            val commands: MutableList<EmbedField> = mutableListOf()

            commands.add(EmbedField("!ping", "Ping bot", false))
            commands.add(EmbedField("!new", "Create a new response", false))
            commands.add(EmbedField("!list", "List all responses", false))
            commands.add(EmbedField("!delete", "Delete a response", false))

            channel(message.channelId).sendReply(message, embed = Embed.new("hElP mE", "(you are retarded and cant write single fucking command)", commands))
        }
    }
}

suspend fun assertAdmin(message: Message, context: BotContext) {
    with(context) {
        val author = message.guild!!.getMember(message.authorId)
        println("2 $author")
        val roles = message.guild!!.getRoles()
        println("3 $roles")
        val rolesString = roles.filter { it.permissions.contains(com.jessecorbett.diskord.api.common.Permission.ADMINISTRATOR) }.map { println("${it.name} is admin"); it.id }
        println("4 $roles")
        val isAdmin = author.roleIds.any { rolesString.contains(it) }
        if (!isAdmin) throw SussyException("fuck you")
    }
}
