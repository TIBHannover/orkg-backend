package org.orkg.graph.output

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.Optional

interface StatementReadRepository : EntityRepository<GeneralStatement, StatementId> {
    fun count(): Long

    fun countStatementsInPaperSubgraph(id: ThingId): Long // Subject id

    fun countAllIncomingStatementsById(ids: Set<ThingId>): Map<ThingId, Long>

    fun countIncomingStatementsById(id: ThingId): Long

    fun fetchAsBundle(
        id: ThingId,
        configuration: BundleConfiguration,
        sort: Sort,
    ): Iterable<GeneralStatement>

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
        objectLabel: String? = null,
    ): Page<GeneralStatement>

    fun findAllByStatementIdIn(
        ids: Set<StatementId>,
        pageable: Pageable,
    ): Page<GeneralStatement>

    fun findAllBySubjectClassAndDOI(
        subjectClass: ThingId,
        doi: String,
        pageable: Pageable,
    ): Page<Resource>

    fun findAllContributorsByResourceId(
        id: ThingId,
        pageable: Pageable,
    ): Page<ContributorId>

    fun findAllDescriptionsById(ids: Set<ThingId>): Map<ThingId, String>

    fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable,
    ): Page<Resource>

    fun findAllProblemsByObservatoryId(
        id: ObservatoryId,
        pageable: Pageable,
    ): Page<Resource>

    fun findAllProblemsByOrganizationId(
        id: OrganizationId,
        pageable: Pageable,
    ): Page<Resource>

    fun findByDOI(
        doi: String,
        classes: Set<ThingId>,
    ): Optional<Resource>

    fun findByStatementId(id: StatementId): Optional<GeneralStatement>

    fun findDOIByContributionId(id: ThingId): Optional<Literal>

    fun findTimelineByResourceId(
        id: ThingId,
        pageable: Pageable,
    ): Page<ResourceContributor>
}

interface StatementWriteRepository {
    fun delete(statement: GeneralStatement)

    fun deleteAll()

    fun deleteByStatementId(id: StatementId)

    fun deleteByStatementIds(ids: Set<StatementId>)

    fun nextIdentity(): StatementId

    fun save(statement: GeneralStatement)

    fun saveAll(statements: Set<GeneralStatement>)
}

interface StatementRepository :
    StatementReadRepository,
    StatementWriteRepository
