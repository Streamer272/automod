package bot

import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotContext

data class Command(val aliases: List<String>, val help: String, val needsAdmin: Boolean = false, val display: Boolean = true, val block: suspend BotContext.(Message) -> Unit)

fun List<Command>.findBy(message: String): Command? {
    val command = message.substring(1).split(" ")[0]
    return this.firstOrNull { it.aliases.contains(command.lowercase()) }
}
