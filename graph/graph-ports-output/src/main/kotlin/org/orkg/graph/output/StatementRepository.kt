package org.orkg.graph.output

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional

interface StatementRepository : EntityRepository<GeneralStatement, StatementId> {
    fun countIncomingStatements(id: ThingId): Long
    fun countIncomingStatements(ids: Set<ThingId>): Map<ThingId, Long>
    fun findAllDescriptions(ids: Set<ThingId>): Map<ThingId, String>
    fun determineOwnership(statementIds: Set<StatementId>): Set<OwnershipInfo>
    // legacy methods:
    fun nextIdentity(): StatementId
    @Transactional
    fun save(statement: GeneralStatement)
    @Transactional
    fun saveAll(statements: Set<GeneralStatement>)
    fun count(): Long
    fun delete(statement: GeneralStatement)
    fun deleteByStatementId(id: StatementId)
    fun deleteByStatementIds(ids: Set<StatementId>)
    fun deleteAll()
    fun findByStatementId(id: StatementId): Optional<GeneralStatement>
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
    fun findAllByStatementIdIn(ids: Set<StatementId>, pageable: Pageable): Page<GeneralStatement>
    fun countByIdRecursive(id: ThingId): Long // Subject id
    fun findAllBySubjects(subjectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObjects(objectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement>
    fun fetchAsBundle(id: ThingId, configuration: BundleConfiguration, sort: Sort): Iterable<GeneralStatement>
    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>
    fun findDOIByContributionId(id: ThingId): Optional<Literal>

    fun findByDOI(doi: String, classes: Set<ThingId>): Optional<Resource>
    fun findAllBySubjectClassAndDOI(subjectClass: ThingId, doi: String, pageable: Pageable): Page<Resource>

    fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>
    fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>

    fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource>
}

data class OwnershipInfo(
    val statementId: StatementId,
    val owner: ContributorId,
)
