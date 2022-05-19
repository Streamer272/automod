package response

import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import helpers.all
import helpers.cacheTransaction
import helpers.getArgs
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class NewResponseArgs(parser: ArgParser) {
    val trigger by parser.positional("TRIGGER", help = "Response trigger")
    val response by parser.positional("RESPONSE", help = "Response")
}

class ListResponseArgs(parser: ArgParser) {
    val match by parser.positional("MATCH", help = "Match response").default("")
    val limit by parser.storing("-l", "--limit", help = "Limit").default(0)
    val offset by parser.storing("-o", "--offset", help = "Offset").default(0)
}

class DeleteResponseArgs(parser: ArgParser) {
    val match by parser.positional("MATCH", help = "Match response")
}

fun initCache() {
    val responses = transaction {
        Response.all().toList()
    }
    cacheTransaction {
        for (response in responses) {
            client.set("response@${response.guildId}:${response.trigger}", response.response)
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
    cacheTransaction {
        client.set("response@${message.guildId}:${args.trigger}", args.response)
    }
}

fun list(message: Message): List<Response> {
    val args = ArgParser(getArgs(message.content)).parseInto(::ListResponseArgs)
    val query = "%${args.match}%"

    return transaction {
        var response =
            Response.find { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger like query or (ResponseTable.response like query)) }
        if (args.limit != 0) response =
            response.limit(args.limit.toString().toInt(), offset = args.offset.toString().toLongOrNull() ?: 0)
        response.toList()
    }
}

fun respond(message: Message): List<String> {
    val cached = cacheTransaction {
        val results = client.all("response@${message.guildId!!}:*") ?: return@cacheTransaction null
        results.filter {
            val trigger = it.key.split(":").toMutableList()
            trigger.removeAt(0)
            message.content.lowercase().contains(trigger.joinToString(":").lowercase())
        }.map { it.value }
    }
    if (cached != null) {
        return cached
    }

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

        val responses = mutableListOf<String>()
        while (result.next()) {
            responses += result.getString("response")
        }

        responses
    }
}

fun delete(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::DeleteResponseArgs)
    val query = "%${args.match}%"

    transaction {
        ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! and (ResponseTable.trigger like query) }
    }
    cacheTransaction {
        val keys = client.keys("response@${message.guildId}:*${args.match}*")
        client.del(keys.joinToString(" "))
    }
}

fun clean(message: Message) {
    transaction {
        ResponseTable.deleteWhere { ResponseTable.guildId eq message.guildId!! }
    }
    cacheTransaction {
        client.keys("response@${message.guildId}:*").forEach {
            client.del(it)
        }
    }
}
