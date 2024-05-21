package org.orkg.graph.input

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
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
    fun findAll(
        pageable: Pageable,
        subjectClasses: Set<ThingId> = emptySet(),
        subjectId: ThingId? = null,
        subjectLabel: String? = null,
        predicateId: ThingId? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        objectClasses: Set<ThingId> = emptySet(),
        objectId: ThingId? = null,
        objectLabel: String? = null
    ): Page<GeneralStatement>
    fun findById(statementId: StatementId): Optional<GeneralStatement>
    fun totalNumberOfStatements(): Long
    fun countStatements(paperId: ThingId): Long

    fun fetchAsBundle(thingId: ThingId, configuration: BundleConfiguration, includeFirst: Boolean, sort: Sort): Bundle

    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>

    fun countIncomingStatements(id: ThingId): Long

    fun countIncomingStatements(ids: Set<ThingId>): Map<ThingId, Long>
}
