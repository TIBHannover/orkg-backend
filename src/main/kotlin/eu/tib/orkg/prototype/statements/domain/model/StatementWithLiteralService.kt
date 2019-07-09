package eu.tib.orkg.prototype.statements.domain.model

import java.util.Optional

/**
 * A service dealing with statements that have literals in the object position.
 */
interface StatementWithLiteralService {
    /**
     * List all statements with literal objects.
     */
    fun findAll(): Iterable<StatementWithLiteral>

    /**
     * Find statement by ID.
     */
    fun findById(statementId: StatementId): Optional<StatementWithLiteral>

    /**
     * Find all statements with a given subject.
     */
    fun findAllBySubject(resourceId: ResourceId): Iterable<StatementWithLiteral>

    /**
     * Find all statements with a given predicate.
     */
    fun findAllByPredicate(predicateId: PredicateId): Iterable<StatementWithLiteral>

    /**
     * Find all statements with a given (literal) object.
     */
    fun findAllByObject(objectId: LiteralId): Iterable<StatementWithLiteral>

    /**
     * Find all statements with a given subject and predicate.
     */
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ): Iterable<StatementWithLiteral>

    /**
     * Create a new statement with a resource as object.
     */
    @Suppress("Reformat")
    fun create(subject: ResourceId, predicate: PredicateId, `object`: LiteralId): StatementWithLiteral

    /**
     * Determine the total number of statements.
     */
    fun totalNumberOfStatements(): Long
}
