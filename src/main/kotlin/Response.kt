import com.jessecorbett.diskord.api.common.Message
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import entities.Response
import org.jetbrains.exposed.sql.transactions.transaction

class ResponseArgs(parser: ArgParser) {
    val trigger by parser.positional("TRIGGER", help = "Response trigger")
    val regex by parser.flagging("-R", "--regex", help = "Trigger is regex").default(false)
    val caseSensitive by parser.flagging("-c", "--case-sensitive", help = "Trigger is case sensitive").default(false)
    val response by parser.positional("RESPONSE", help = "Response")
}

fun newResponse(message: Message): Response {
    val args = ArgParser(getArgs(message.content)).parseInto(::ResponseArgs)

    println("regex ${args.regex} caseSensitive ${args.caseSensitive}")

    val response = transaction {
        Response.new {
            trigger = args.trigger
            regex = args.regex
            caseSensitive = args.caseSensitive
            response = args.response
        }
    }

    return response
}
