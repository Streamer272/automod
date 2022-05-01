package entities

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object ResponseTable : UUIDTable() {
    val trigger = varchar("trigger", 256).uniqueIndex()
    val regex = bool("regex").default(false)
    val response = varchar("response", 512)
}

class Response(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Response>(ResponseTable)

    var trigger by ResponseTable.trigger
    val regex by ResponseTable.regex
    var response by ResponseTable.response
}
