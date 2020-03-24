package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Pageable

interface PredicateService {
    /**
     * Create a new predicate with a given label.
     */
    fun create(label: String): Predicate

    /**
     * Create a new predicate with a given label for a given user.
     */
    fun create(userId: UUID, label: String): Predicate

    /**
     * Create a new predicate from a request.
     */
    fun create(request: CreatePredicateRequest): Predicate

    /**
     * Create a new predicate from a request for a given user.
     */
    fun create(userId: UUID, request: CreatePredicateRequest): Predicate

    /**
     * List all predicates.
     */
    fun findAll(pageable: Pageable): Iterable<Predicate>

    /**
     * Find a predicate by its ID.
     */
    fun findById(id: PredicateId?): Optional<Predicate>

    /**
     * Find all predicates matching a label.
     */
    fun findAllByLabel(label: String, pageable: Pageable): Iterable<Predicate>

    /**
     * Find all predicates matching a label partially.
     */
    fun findAllByLabelContaining(part: String, pageable: Pageable): Iterable<Predicate>

    /**
     * Update a predicate.
     */
    fun update(predicate: Predicate): Predicate
}
