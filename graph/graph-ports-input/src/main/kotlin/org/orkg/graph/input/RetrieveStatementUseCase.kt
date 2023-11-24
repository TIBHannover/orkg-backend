package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.StatementId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

interface RetrieveStatementUseCase {
    fun exists(id: StatementId): Boolean
    // legacy methods:
    fun findAll(pagination: Pageable): Page<GeneralStatement>
    fun findById(statementId: StatementId): Optional<GeneralStatement>
    fun findAllBySubject(subjectId: ThingId, pagination: Pageable): Page<GeneralStatement>
    fun findAllByPredicate(predicateId: ThingId, pagination: Pageable): Page<GeneralStatement>
    fun findAllByObject(objectId: ThingId, pagination: Pageable): Page<GeneralStatement>
    fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun totalNumberOfStatements(): Long
    fun countStatements(paperId: ThingId): Long
    fun findAllByPredicateAndLabel(
        predicateId: ThingId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun fetchAsBundle(thingId: ThingId, configuration: BundleConfiguration, includeFirst: Boolean, sort: Sort): Bundle

    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>

    fun countStatementsAboutResource(id: ThingId): Long

    fun countStatementsAboutResources(ids: Set<ThingId>): Map<ThingId, Long>
}
