package joke

import bot.getArgs
import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import response.ResponseTable
import java.util.*

class NewJokeArgs(parser: ArgParser) {
    val name by parser.positional("NAME", help = "Joke name")
    val joke by parser.positional("JOKE", help = "Joke")
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
