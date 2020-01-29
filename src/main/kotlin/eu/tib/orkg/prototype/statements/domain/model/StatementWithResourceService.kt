package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

/**
 * A service dealing with statements that have resources in the object position.
 */
interface StatementWithResourceService {
    /**
     * List all statements with resource objects.
     */
    fun findAll(pagination: Pageable): Iterable<StatementWithResource>

    /**
     * Find statement by ID.
     */
    fun findById(statementId: StatementId): Optional<StatementWithResource>

    /**
     * Find all statements with a given subject.
     */
    fun findAllBySubject(resourceId: ResourceId, pagination: Pageable): Iterable<StatementWithResource>

    /**
     * Find all statements with a given predicate.
     */
    fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<StatementWithResource>

    /**
     * Find all statements with a given (resource) object.
     */
    fun findAllByObject(objectId: ResourceId, pagination: Pageable): Iterable<StatementWithResource>

    /**
     * Find all statements with a given subject and predicate.
     */
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<StatementWithResource>

    /**
     * Create a new statement with a resource as object.
     */
    @Suppress("Reformat")
    fun create(subject: ResourceId, predicate: PredicateId, `object`: ResourceId): StatementWithResource

    /**
     * Create a new statement with a resource as object belonging to a given user.
     */
    @Suppress("Reformat")
    fun create(userId: UUID, subject: ResourceId, predicate: PredicateId, `object`: ResourceId): StatementWithResource

    /**
     * Determine the total number of statements.
     */
    fun totalNumberOfStatements(): Long

    /**
     * Removes a resource statement
     */
    fun remove(statementId: StatementId)

    fun countStatements(paperId: ResourceId?): Int

    /**
     * updates a statement
     */
    fun update(statementEditRequest: StatementEditRequest): StatementWithResource
}
