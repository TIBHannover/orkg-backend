package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime

data class Predicate(
    val id: PredicateId?,
    val label: String,
    val createdAt: OffsetDateTime?
)
