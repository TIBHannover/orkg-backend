package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import java.util.Optional

interface PredicateService {
    /**
     * Create a new predicate with a given label.
     */
    fun create(label: String): Predicate

    /**
     * Create a new predicate from a request.
     */
    fun create(request: CreatePredicateRequest): Predicate

    /**
     * List all predicates.
     */
    fun findAll(): Iterable<Predicate>

    /**
     * Find a predicate by its ID.
     */
    fun findById(id: PredicateId?): Optional<Predicate>

    /**
     * Find all predicates matching a label.
     */
    fun findAllByLabel(label: String): Iterable<Predicate>

    /**
     * Find all predicates matching a label partially.
     */
    fun findAllByLabelContaining(part: String): Iterable<Predicate>
}
