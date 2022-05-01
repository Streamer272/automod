package response

import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import getArgs
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction

class NewResponseArgs(parser: ArgParser) {
    val trigger by parser.positional("TRIGGER", help = "Response trigger")
    val caseSensitive by parser.flagging("-c", "--case-sensitive", help = "Trigger is case sensitive").default(false)
    val response by parser.positional("RESPONSE", help = "Response")
}

class ListResponseArgs(parser: ArgParser) {
    val match by parser.positional("MATCH", help = "Match response").default("")
    val regex by parser.flagging("-R", "--regex", help = "Match is regex").default(false)
    val limit by parser.storing("-l", "--limit", help = "Limit").default(0)
    val offset by parser.storing("-o", "--offset", help = "Offset").default(0)
}

class DeleteResponseArgs(parser: ArgParser) {
    val match by parser.positional("MATCH", help = "Match response")
    val regex by parser.flagging("-R", "--regex", help = "Match is regex").default(false)
}

fun new(message: Message): Response {
    val args = ArgParser(getArgs(message.content)).parseInto(::NewResponseArgs)

    val response = transaction {
        Response.new {
            trigger = args.trigger
            caseSensitive = args.caseSensitive
            response = args.response
            guildId = message.guildId!!
        }
    }

    return response
}

fun list(message: Message): List<Response> {
    val args = ArgParser(getArgs(message.content)).parseInto(::ListResponseArgs)
    val match = when (args.regex) {
        true -> args.match
        false -> "%${args.match}%"
    }

    return if (!args.regex) {
        transaction {
            var response =
                Response.find { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger like match or (ResponseTable.response like match)) }
            if (args.limit != 0) {
                response = response.limit(args.limit as Int, offset = args.offset as Long)
            }
            response.toList()
        }
    } else {
        transaction {
            var response = Response.all()
            if (args.limit != 0) {
                response = response.limit(args.limit as Int, offset = args.offset as Long)
            }
            response.filter { it.trigger.matches(match.toRegex()) or it.response.matches(match.toRegex()) }.toList()
        }
    }
}

fun respond(message: Message): List<Response> {
    return transaction {
        val responses = Response.all().filter { it.trigger in message.content }
        if (responses.size > 10) responses.subList(0, 10) else responses
    }
}

fun delete(message: Message): Response? {
    val args = ArgParser(getArgs(message.content)).parseInto(::DeleteResponseArgs)
    val match = when (args.regex) {
        true -> args.match
        false -> "%${args.match}%"
    }

    return if (!args.regex) {
        transaction {
            val response =
                Response.find { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger like match) }
                    .firstOrNull()
            response?.delete()
            response
        }
    } else {
        transaction {
            val response = Response.find { ResponseTable.guildId eq message.guildId!! }
                .firstOrNull { it.trigger.matches(match.toRegex()) }
            response?.delete()
            response
        }
    }
}

fun clean(message: Message) {
    transaction {
        ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! }
    }
}
