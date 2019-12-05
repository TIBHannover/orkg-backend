package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.NewStatementEditRequest
import org.springframework.data.domain.Pageable
import java.util.Optional

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
     * Create a new statement.
     */
    @Suppress("Reformat")
    fun create(subject: String, predicate: PredicateId, `object`: String): GeneralStatement

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
    fun update(statementEditRequest: NewStatementEditRequest): GeneralStatement
}
