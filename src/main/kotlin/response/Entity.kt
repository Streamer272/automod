package response

import com.jessecorbett.diskord.api.common.Embed
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ResponseTable : UUIDTable() {
    val trigger = varchar("trigger", 256).uniqueIndex()
    val regex = bool("regex").default(false)
    val caseSensitive = bool("case_sensitive").default(false)
    val response = varchar("response", 512)
    val guildId = varchar("guild_id", 64)
}

class Response(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Response>(ResponseTable)

    var trigger by ResponseTable.trigger
    var regex by ResponseTable.regex
    var caseSensitive by ResponseTable.caseSensitive
    var response by ResponseTable.response
    var guildId by ResponseTable.guildId

    override fun toString(): String {
        return Json.encodeToString(ResponseDTO(
            id = id.value.toString(),
            trigger = trigger,
            regex = regex,
            caseSensitive = caseSensitive,
            response = response,
            guildId = guildId
        ))
    }

    fun toEmbed(): Embed {
        return Embed(
            
        )
    }
}

@Serializable
data class ResponseDTO(
    val id: String,
    val trigger: String,
    val regex: Boolean,
    val caseSensitive: Boolean,
    val response: String,
    val guildId: String
)
