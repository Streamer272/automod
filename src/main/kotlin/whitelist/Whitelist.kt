package whitelist

import bot.getArgs
import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import response.ResponseTable

class EditWhitelistArgs(parser: ArgParser) {
    val user by parser.positional("USER", help = "@somebody")
}

fun add(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::EditWhitelistArgs)
    val extractedUserId = extractUserId(args.user)

    transaction {
        Whitelist.new {
            userId = extractedUserId
            guildId = message.guildId!!
        }
    }
}

fun remove(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::EditWhitelistArgs)
    val extractedUserId = extractUserId(args.user)

    transaction {
        WhitelistTable.deleteWhere { WhitelistTable.guildId eq message.guildId!! and (WhitelistTable.userId eq extractedUserId) }
    }
}

fun clean(message: Message) {
    transaction {
        ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! }
    }
}
