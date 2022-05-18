package helpers

import bot.SusException

fun extractPing(ping: String): String {
    val regex = Regex("<@\\d{10,64}>")
    if (!regex.containsMatchIn(ping)) throw SusException("you need to @ping somebody idiot")

    return ping
        .replace("<@", "")
        .replace(">", "")
}
