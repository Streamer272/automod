package joke

import bot.customEmbed
import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedField
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object JokeTable : UUIDTable("joke") {
    val name = varchar("name", 64).uniqueIndex()
    val joke = varchar("joke", 2048)
    val guildId = varchar("guild_id", 64)
}

class Joke(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Joke>(JokeTable)

    var name by JokeTable.name
    var joke by JokeTable.joke
    var guildId by JokeTable.guildId
}

fun List<Joke>.toEmbed(): Embed {
    return Embed.customEmbed(
        null,
        null,
        this.map {
            EmbedField(
                name = it.name,
                value = when(it.joke.length) {
                    in 0..64 -> it.joke
                    else -> "${it.joke.substring(0..61)}..."
                },
                inline = false
            )
        } as MutableList<EmbedField>
    )
}
