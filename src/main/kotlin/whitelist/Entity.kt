package whitelist

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object WhitelistTable : UUIDTable("whitelist") {
    val userId = varchar("user_id", 64)
}

class Whitelist(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Whitelist>(WhitelistTable)

    var userId by WhitelistTable.userId
}
