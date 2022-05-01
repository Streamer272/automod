package response

import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import new
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ResponseTable : UUIDTable() {
    val trigger = varchar("trigger", 64).uniqueIndex()
    val caseSensitive = bool("case_sensitive").default(false)
    val response = varchar("response", 2048)
    val guildId = varchar("guild_id", 64)
}

class Response(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Response>(ResponseTable)

    var trigger by ResponseTable.trigger
    var caseSensitive by ResponseTable.caseSensitive
    var response by ResponseTable.response
    var guildId by ResponseTable.guildId
}

fun List<Response>.toEmbed(): Embed {
    return Embed.new(
        null,
        null,
        this.map {
            EmbedField(
                name = it.trigger,
                value = """
                Case Sensitive: ${it.caseSensitive}
                Response: ${it.response}
                """.trimIndent(),
                inline = false
            )
        } as MutableList<EmbedField>,
    )
}
