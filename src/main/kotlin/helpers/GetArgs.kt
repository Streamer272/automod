package helpers

fun getArgs(message: String): Array<String> {
    val args = mutableListOf<String>()

    var current = ""
    var inQuote = false
    var escaped = false

    for (ch in message) {
        if (ch == '\\') {
            escaped = true
            continue
        } else if (ch == '"' && !escaped) {
            inQuote = !inQuote
        } else if (ch == ' ' && !inQuote && !escaped) {
            args.add(current)
            current = ""
        } else {
            current += ch
        }

        if (escaped) {
            escaped = false
        }
    }

    if (current.isNotEmpty()) {
        args.add(current)
    }
    args[0].let {
        if (it.startsWith("!")) {
            args.remove(it)
        }
    }

    return args.toTypedArray()
}
