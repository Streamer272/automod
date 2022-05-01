package entities

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
}

class Response(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Response>(ResponseTable)

    var trigger by ResponseTable.trigger
    var regex by ResponseTable.regex
    var caseSensitive by ResponseTable.caseSensitive
    var response by ResponseTable.response
}
