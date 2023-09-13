package eu.tib.orkg.prototype.statements.spi

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.transaction.annotation.Transactional

interface StatementRepository : EntityRepository<GeneralStatement, StatementId> {
    fun countStatementsAboutResource(id: ThingId): Long
    fun countStatementsAboutResources(resourceIds: Set<ThingId>): Map<ThingId, Long>
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
    fun findAllByStatementIdIn(ids: Set<StatementId>, pageable: Pageable): Page<GeneralStatement>
    fun findAllBySubject(subjectId: ThingId, pageable: Pageable): Page<GeneralStatement>
    fun findAllByPredicateId(predicateId: ThingId, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObject(objectId: ThingId, pageable: Pageable): Page<GeneralStatement>
    fun countByIdRecursive(id: ThingId): Long // Subject id
    fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabel(
        predicateId: ThingId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjects(subjectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObjects(objectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement>
    fun fetchAsBundle(id: ThingId, configuration: BundleConfiguration, sort: Sort): Iterable<GeneralStatement>
    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>
    fun findDOIByContributionId(id: ThingId): Optional<Literal>
    fun countPredicateUsage(id: ThingId): Long

    /** Find any resource by DOI. */
    fun findByDOI(doi: String): Optional<Resource>
    fun findAllBySubjectClassAndDOI(subjectClass: ThingId, doi: String, pageable: Pageable): Page<Resource>

    fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>
    fun checkIfResourceHasStatements(id: ThingId): Boolean
    fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
    fun findBySubjectIdAndPredicateIdAndObjectId(subjectId: ThingId, predicateId: ThingId, objectId: ThingId): Optional<GeneralStatement>
}

@QueryResult
data class ResourceContributor(
    @JsonProperty("created_by")
    val createdBy: String, // FIXME: This should be ContributorId
    @JsonProperty("created_at")
    val createdAt: String // FIXME: This should be OffsetDateTime
)

data class OwnershipInfo(
    val statementId: StatementId,
    val owner: ContributorId,
)
