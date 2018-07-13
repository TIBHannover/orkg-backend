package eu.tib.orkg.prototype.statements.domain.model

import java.time.LocalDateTime

data class Predicate(
    val id: PredicateId?,
    val label: String
) {
    val created: LocalDateTime = LocalDateTime.now()
}
