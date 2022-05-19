package response

import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import helpers.cacheTransaction
import helpers.getArgs
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class NewResponseArgs(parser: ArgParser) {
    val trigger by parser.positional("TRIGGER", help = "Response trigger")
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

fun initCache() {
    val responses = transaction {
        Response.all().toList()
    }
    cacheTransaction {
        for (response in responses) {
            client.set("${response.guildId}:${response.trigger}", response.response)
        }
    }
}

fun new(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::NewResponseArgs)

    transaction {
        Response.new {
            trigger = args.trigger
            response = args.response
            guildId = message.guildId!!
        }
    }
}

fun list(message: Message): List<Response> {
    val args = ArgParser(getArgs(message.content)).parseInto(::ListResponseArgs)
    val query = when (args.regex) {
        true -> args.match
        false -> "%${args.match}%"
    }

    return if (!args.regex) {
        transaction {
            var response =
                Response.find { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger like query or (ResponseTable.response like query)) }
            if (args.limit != 0) response =
                response.limit(args.limit.toString().toInt(), offset = args.offset.toString().toLongOrNull() ?: 0)
            response.toList()
        }
    } else {
        transaction {
            var response =
                Response.find { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger match (query) or (ResponseTable.response match (query))) }
            if (args.limit != 0) response =
                response.limit(args.limit.toString().toInt(), offset = args.offset.toString().toLongOrNull() ?: 0)
            response.toList()
        }
    }
}

fun respond(message: Message): List<Response> {
    return transaction {
        val conn = TransactionManager.current().connection
        val statement = conn.prepareStatement(
            "SELECT * FROM response WHERE ? LIKE '%' || LOWER(trigger) || '%' AND guild_id = ?",
            false
        )
        statement.fillParameters(
            listOf(
                Pair(VarCharColumnType(), message.content.lowercase()),
                Pair(VarCharColumnType(), message.guildId)
            )
        )
        val result = statement.executeQuery()

        val responses = mutableListOf<Response>()
        var i = 0
        while (result.next() && i++ < 10) {
            val id = result.getString("id")
            val uuid = UUID.fromString(id)
            responses += Response.findById(uuid)!!
        }

        responses
    }
}

fun delete(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::DeleteResponseArgs)
    val query = when (args.regex) {
        true -> args.match
        false -> "%${args.match}%"
    }

    if (!args.regex) {
        transaction {
            ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger like query) }
        }
    } else {
        transaction {
            ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger match (query)) }
        }
    }
}

fun clean(message: Message) {
    transaction {
        ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! }
    }
}
