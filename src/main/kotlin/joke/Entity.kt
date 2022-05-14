package joke

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object JokeTable : UUIDTable("joke") {
    val joke = varchar("joke", 2048)
    val guildId = varchar("guild_id", 64)
}

class Joke(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Joke>(JokeTable)

    var joke by JokeTable.joke
    var guildId by JokeTable.guildId
}
