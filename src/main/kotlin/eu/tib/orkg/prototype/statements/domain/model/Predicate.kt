package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.authentication.domain.model.UserId
import java.time.LocalDateTime

data class Predicate(
    val id: PredicateId,
    val userId: UserId,
    val createdAt: LocalDateTime
)
