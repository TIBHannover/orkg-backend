package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.application.BundleConfiguration
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveStatementUseCase {
    fun exists(id: StatementId): Boolean
    // legacy methods:
    fun findAll(pagination: Pageable): Iterable<StatementRepresentation>
    fun findById(statementId: StatementId): Optional<StatementRepresentation>
    fun findAllBySubject(subjectId: String, pagination: Pageable): Page<StatementRepresentation>
    fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<StatementRepresentation>
    fun findAllByObject(objectId: String, pagination: Pageable): Page<StatementRepresentation>
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<StatementRepresentation>

    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<StatementRepresentation>

    fun totalNumberOfStatements(): Long
    fun countStatements(paperId: String): Int
    fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<StatementRepresentation>

    fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pagination: Pageable
    ): Iterable<StatementRepresentation>

    fun fetchAsBundle(thingId: String, configuration: BundleConfiguration, includeFirst: Boolean): Bundle
}
