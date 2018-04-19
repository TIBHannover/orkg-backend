package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.authentication.domain.model.UserId
import java.time.LocalDateTime

data class SubjectNode(
    val id: SubjectId,
    val userId: UserId,
    val createdAt: LocalDateTime
)
