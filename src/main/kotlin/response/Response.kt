package response

import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import getArgs
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction

class NewResponseArgs(parser: ArgParser) {
    val trigger by parser.positional("TRIGGER", help = "Response trigger")
    val regex by parser.flagging("-R", "--regex", help = "Trigger is regex").default(false)
    val caseSensitive by parser.flagging("-c", "--case-sensitive", help = "Trigger is case sensitive").default(false)
    val response by parser.positional("RESPONSE", help = "Response")
}

class ListResponseArgs(parser: ArgParser) {
    val match by parser.positional("MATCH", help = "Match response").default("")
    val regex by parser.flagging("-R", "--regex", help = "Match is regex").default(false)
    val limit by parser.storing("-l", "--limit", help = "Limit").default(Int.MAX_VALUE)
    val offset by parser.storing("-o", "--offset", help = "Offset").default(0)
}

fun new(message: Message): Response {
    val args = ArgParser(getArgs(message.content)).parseInto(::NewResponseArgs)

    val response = transaction {
        Response.new {
            trigger = args.trigger
            regex = args.regex
            caseSensitive = args.caseSensitive
            response = args.response
            guildId = message.guildId!!
        }
    }

    return response
}

fun list(message: Message): List<Response> {
    val args = ArgParser(getArgs(message.content)).parseInto(::ListResponseArgs)
    val match = "%${args.match}%"

    return if (!args.regex)
        transaction {
            Response.find { ResponseTable.trigger like match or (ResponseTable.response like match) }
                .limit(args.limit as Int, offset = args.offset as Long)
                .toList()
        }
    else
        transaction {
            Response.all()
                .limit(args.limit as Int, offset = args.offset as Long)
                .filter { it.trigger.matches(match.toRegex()) or it.response.matches(match.toRegex()) }
                .toList()
        }
}
