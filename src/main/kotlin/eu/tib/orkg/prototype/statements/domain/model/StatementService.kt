package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Pageable

/**
 * A service dealing with general statements.
 */
interface StatementService {
    /**
     * List all statements.
     */
    fun findAll(pagination: Pageable): Iterable<GeneralStatement>

    /**
     * Find statement by ID.
     */
    fun findById(statementId: StatementId): Optional<GeneralStatement>

    /**
     * Find all statements with a given subject.
     */
    fun findAllBySubject(subjectId: String, pagination: Pageable): Iterable<GeneralStatement>

    /**
     * Find all statements with a given predicate.
     */
    fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<GeneralStatement>

    /**
     * Find all statements by object.
     */
    fun findAllByObject(objectId: String, pagination: Pageable): Iterable<GeneralStatement>

    /**
     * Find all statements with a given subject and predicate.
     */
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<GeneralStatement>

    /**
     * Find all statements with a given object and predicate.
     */
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<GeneralStatement>

    /**
     * Create a new statement.
     */
    @Suppress("Reformat")
    fun create(subject: String, predicate: PredicateId, `object`: String): GeneralStatement

    /**
     * Create a new statement with the created_by user.
     */
    @Suppress("Reformat")
    fun create(userId: UUID, subject: String, predicate: PredicateId, `object`: String): GeneralStatement

    /**
     * Determine the total number of statements.
     */
    fun totalNumberOfStatements(): Long

    /**
     * Removes a statement
     */
    fun remove(statementId: StatementId)

    /**
     * updates a statement
     */
    fun update(statementEditRequest: StatementEditRequest): GeneralStatement

    /**
     * Count statements (for widget)
     */
    fun countStatements(paperId: String): Int

    /**
     * Get a bundle of statements
     * which represents the entire sub-graph starting from a [Thing]
     */
    fun fetchAsBundle(thingId: String): Bundle
}
