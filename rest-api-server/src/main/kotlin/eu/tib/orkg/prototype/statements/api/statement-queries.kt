package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.application.BundleConfiguration
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveStatementUseCase {
    // legacy methods:
    fun findAll(pagination: Pageable): Iterable<GeneralStatement>
    fun findById(statementId: StatementId): Optional<GeneralStatement>
    fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement>
    fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement>
    fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement>
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<GeneralStatement>

    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<GeneralStatement>

    fun totalNumberOfStatements(): Long
    fun countStatements(paperId: String): Int
    fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Iterable<GeneralStatement>

    fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Iterable<GeneralStatement>

    fun fetchAsBundle(thingId: String, configuration: BundleConfiguration, includeFirst: Boolean): Bundle
}
