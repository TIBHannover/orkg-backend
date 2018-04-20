package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

interface PredicateRepository {

    fun findAll(): Iterable<PredicateId>

    fun findById(id: PredicateId): Optional<Predicate>

    fun add(predicate: Predicate)
}
