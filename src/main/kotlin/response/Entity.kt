package response

import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import bot.customEmbed
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ResponseTable : UUIDTable("response") {
    val trigger = varchar("trigger", 64)
    val response = varchar("response", 2048)
    val guildId = varchar("guild_id", 64)
}

class Response(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Response>(ResponseTable)

    var trigger by ResponseTable.trigger
    var response by ResponseTable.response
    var guildId by ResponseTable.guildId
}

fun List<Response>.toEmbed(): Embed {
    return Embed.customEmbed(
        null,
        null,
        this.map {
            EmbedField(
                name = it.trigger,
                value = when(it.response.length) {
                    in 0..256 -> it.response
                    else -> "${it.response.substring(0..254)}..."
                },
                inline = false
            )
        } as MutableList<EmbedField>,
    )
}
