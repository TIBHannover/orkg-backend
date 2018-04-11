package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.core.Entity
import java.time.LocalDateTime

sealed class ObjectNode {
    data class ObjectEntity(
        override val id: ObjectId,
        val userId: String,
        val createdAt: LocalDateTime
    ) :
        Entity<ObjectId>(id)

    data class Literal(val value: String)
}
