package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

/**
 * A service dealing with statements that have literals in the object position.
 */
interface StatementWithLiteralService {
    /**
     * List all statements with literal objects.
     */
    fun findAll(pagination: Pageable): Iterable<StatementWithLiteral>

    /**
     * Find statement by ID.
     */
    fun findById(statementId: StatementId): Optional<StatementWithLiteral>

    /**
     * Find all statements with a given subject.
     */
    fun findAllBySubject(resourceId: ResourceId, pagination: Pageable): Iterable<StatementWithLiteral>

    /**
     * Find all statements with a given predicate.
     */
    fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<StatementWithLiteral>

    /**
     * Find all statements with a given (literal) object.
     */
    fun findAllByObject(objectId: LiteralId, pagination: Pageable): Iterable<StatementWithLiteral>

    /**
     * Find all statements with a given subject and predicate.
     */
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<StatementWithLiteral>

    /**
     * Create a new statement with a resource as object.
     */
    @Suppress("Reformat")
    fun create(subject: ResourceId, predicate: PredicateId, `object`: LiteralId): StatementWithLiteral

    /**
     * Create a new statement with a resource as object belonging to a given user.
     */
    @Suppress("Reformat")
    fun create(userId: UUID, subject: ResourceId, predicate: PredicateId, `object`: LiteralId): StatementWithLiteral

    /**
     * Determine the total number of statements.
     */
    fun totalNumberOfStatements(): Long

    /**
     * Removes a literal statement
     */
    fun remove(statementId: StatementId)

    /**
     * updates a statement
     */
    fun update(statementEditRequest: StatementEditRequest): StatementWithLiteral
}
