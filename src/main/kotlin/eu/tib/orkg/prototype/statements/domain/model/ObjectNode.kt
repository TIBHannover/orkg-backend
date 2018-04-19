package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.authentication.domain.model.UserId
import java.time.LocalDateTime

sealed class ObjectNode {
    data class ObjectEntity(
        val id: ResourceId,
        val userId: UserId,
        val createdAt: LocalDateTime
    )

    data class Literal(val value: String)
}
