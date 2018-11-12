package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

interface StatementWithResourceService {
    /**
     * List all statements.
     */
    fun findAll(): Iterable<StatementWithResource>

    /**
     * Find statement by ID.
     */
    fun findById(statementId: Long): Optional<StatementWithResource>

    /**
     * Find all statements with a given subject.
     */
    fun findAllBySubject(resourceId: ResourceId): Iterable<StatementWithResource>

    /**
     * Find all statements with a given predicate.
     */
    fun findAllByPredicate(predicateId: PredicateId): Iterable<StatementWithResource>

    /**
     * Create a new statement with a resource as object.
     */
    @Suppress("Reformat")
    fun create(subject: ResourceId, predicate: PredicateId, `object`: ResourceId): StatementWithResource
}
