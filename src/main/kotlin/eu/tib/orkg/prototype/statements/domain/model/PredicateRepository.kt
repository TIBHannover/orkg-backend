package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

interface PredicateRepository {

    fun findAll(): Iterable<Predicate>

    fun findById(id: PredicateId): Optional<Predicate>

    fun findByLabel(searchString: String): Iterable<Predicate>

    fun add(predicate: Predicate)
}
