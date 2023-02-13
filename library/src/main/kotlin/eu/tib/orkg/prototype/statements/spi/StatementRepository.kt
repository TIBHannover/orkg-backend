package eu.tib.orkg.prototype.statements.spi

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.*
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.transaction.annotation.Transactional

interface StatementRepository : EntityRepository<GeneralStatement, StatementId> {
    fun countStatementsAboutResource(id: ResourceId): Long
    fun countStatementsAboutResources(resourceIds: Set<ResourceId>): Map<ResourceId, Long>
    // legacy methods:
    fun nextIdentity(): StatementId
    @Transactional
    fun save(statement: GeneralStatement)
    fun count(): Long
    fun delete(statement: GeneralStatement)
    fun deleteByStatementId(id: StatementId)
    fun deleteAll()
    fun findAll(depth: Int): Iterable<GeneralStatement>
    fun findByStatementId(id: StatementId): Optional<GeneralStatement>
    fun findAllBySubject(subjectId: String, pageable: Pageable): Page<GeneralStatement>
    fun findAllByPredicateId(predicateId: PredicateId, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObject(objectId: String, pageable: Pageable): Page<GeneralStatement>
    fun countByIdRecursive(paperId: String): Int
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjects(subjectIds: List<String>, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObjects(objectIds: List<String>, pageable: Pageable): Page<GeneralStatement>
    fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement>
    fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount>
    fun findDOIByContributionId(id: ResourceId): Optional<Literal>
    fun countPredicateUsage(id: PredicateId): Long
    fun findByDOI(doi: String): Optional<Resource>
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun findContributorsByResourceId(id: ResourceId, pageable: Pageable): Page<ResourceContributor>
    fun checkIfResourceHasStatements(id: ResourceId): Boolean
    fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
}

@QueryResult
data class ResourceContributor(
    @JsonProperty("created_by")
    val createdBy: String, // FIXME: This should be ContributorId
    @JsonProperty("created_at")
    val createdAt: String // FIXME: This should be OffsetDateTime
)
