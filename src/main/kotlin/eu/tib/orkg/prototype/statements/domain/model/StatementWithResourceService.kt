package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

/**
 * A service dealing with statements that have resources in the object position.
 */
interface StatementWithResourceService {
    /**
     * List all statements with resource objects.
     */
    fun findAll(): Iterable<StatementWithResource>

    /**
     * Find statement by ID.
     */
    fun findById(statementId: StatementId): Optional<StatementWithResource>

    /**
     * Find all statements with a given subject.
     */
    fun findAllBySubject(resourceId: ResourceId): Iterable<StatementWithResource>

    /**
     * Find all statements with a given predicate.
     */
    fun findAllByPredicate(predicateId: PredicateId): Iterable<StatementWithResource>

    /**
     * Find all statements with a given subject and predicate.
     */
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ): Iterable<StatementWithResource>

    /**
     * Create a new statement with a resource as object.
     */
    @Suppress("Reformat")
    fun create(subject: ResourceId, predicate: PredicateId, `object`: ResourceId): StatementWithResource
}
