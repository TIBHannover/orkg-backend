package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.authentication.domain.model.UserId
import eu.tib.orkg.prototype.core.Entity
import java.time.LocalDateTime

data class Predicate(
    override val id: PredicateId,
    val userId: UserId,
    val createdAt: LocalDateTime
) :
    Entity<PredicateId>(id)
