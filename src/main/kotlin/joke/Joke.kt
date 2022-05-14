package joke

import bot.getArgs
import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class NewJokeArgs(parser: ArgParser) {
    val name by parser.positional("NAME", help = "Joke name")
    val joke by parser.positional("JOKE", help = "Joke")
}

class ListJokeArgs(parser: ArgParser) {
    val match by parser.positional("MATCH", help = "Match response").default("")
    val limit by parser.storing("-l", "--limit", help = "Limit").default(0)
    val offset by parser.storing("-o", "--offset", help = "Offset").default(0)
}

class DeleteJokeArgs(parser: ArgParser) {
    val name by parser.positional("NAME", help = "Joke name")
}

fun new(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::NewJokeArgs)

    transaction {
        Joke.new {
            name = args.name
            joke = args.joke
            guildId = message.guildId!!
        }
    }
}

fun list(message: Message): List<Joke> {
    val args = ArgParser(getArgs(message.content)).parseInto(::ListJokeArgs)
    val query = "%${args.match}%"

    return transaction {
        var jokes =
            Joke.find { JokeTable.guildId eq message.guildId!! and (JokeTable.name like query or (JokeTable.joke like query)) }
        if (args.limit != 0) jokes =
            jokes.limit(args.limit.toString().toInt(), offset = args.offset.toString().toLongOrNull() ?: 0)
        jokes.toList()
    }
}

fun respond(message: Message): Joke? {
    return transaction {
        val conn = TransactionManager.current().connection
        val statement = conn.prepareStatement(
            "SELECT * FROM joke WHERE guild_id = ? ORDER BY random() LIMIT 1",
            false
        )
        statement.fillParameters(
            listOf(
                Pair(VarCharColumnType(), message.guildId)
            )
        )
        val result = statement.executeQuery()

        if (!result.next()) return@transaction null
        val id = result.getString("id")
        val uuid = UUID.fromString(id)
        return@transaction Joke.findById(uuid)
    }
}

fun delete(message: Message) {
    val args = ArgParser(getArgs(message.content)).parseInto(::DeleteJokeArgs)

    transaction {
        JokeTable.deleteWhere { JokeTable.guildId eq message.guildId!! and (JokeTable.name eq args.name) }
    }
}

fun random(): Boolean {
    return (0..12).random() == 12
}
