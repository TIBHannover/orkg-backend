package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatement
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThing
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

typealias PredicateLookupTable = Map<ThingId, Predicate>

@Component
class SpringDataNeo4jStatementAdapter(
    private val neo4jRepository: Neo4jStatementRepository,
    private val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    private val neo4jResourceRepository: Neo4jResourceRepository,
    private val neo4jPredicateRepository: Neo4jPredicateRepository,
    private val neo4jLiteralRepository: Neo4jLiteralRepository,
    private val neo4jClassRepository: Neo4jClassRepository,
    private val predicateRepository: PredicateRepository,
) : StatementRepository {
    override fun nextIdentity(): StatementId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: StatementId
        do {
            id = neo4jStatementIdGenerator.nextIdentity()
        } while (neo4jRepository.existsByStatementId(id))
        return id
    }

    override fun save(statement: GeneralStatement) {
        neo4jRepository.save(statement.toNeo4jStatement())
    }

    override fun count(): Long = neo4jRepository.count()

    override fun delete(statement: GeneralStatement) {
        neo4jRepository.deleteByStatementId(statement.id!!)
    }

    override fun deleteByStatementId(id: StatementId) {
        neo4jRepository.deleteByStatementId(id)
    }

    override fun deleteByStatementIds(ids: Set<StatementId>) {
        neo4jRepository.deleteByStatementIds(ids)
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> {
        val neo4jStatements = neo4jRepository.findAll(pageable)
        val predicateIds = neo4jStatements.content.mapNotNull(Neo4jStatement::predicateId).toSet()
        val table = neo4jPredicateRepository.findAllByPredicateIdIn(predicateIds)
            .map(Neo4jPredicate::toPredicate)
            .associateBy { it.id }
        return neo4jStatements.map { it.toStatement(table) }
    }

    override fun countStatementsAboutResource(id: ThingId): Long =
        neo4jRepository.countStatementsByObjectId(id.toResourceId())

    override fun countStatementsAboutResources(resourceIds: Set<ThingId>): Map<ThingId, Long> =
        neo4jRepository.countStatementsAboutResource(resourceIds.toResourceIds()).associate { ThingId(it.resourceId) to it.count }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        neo4jRepository.findByStatementId(id).map { it.toStatement() }

    override fun findAllBySubject(subjectId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubject(subjectId, pageable).map { it.toStatement() }

    override fun findAllByPredicateId(predicateId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateId(predicateId.toPredicateId(), pageable).map { it.toStatement() }

    override fun findAllByObject(objectId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObject(objectId, pageable).map { it.toStatement() }

    override fun countByIdRecursive(id: ThingId): Long = neo4jRepository.countByIdRecursive(id)

    override fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByObjectAndPredicate(objectId, predicateId.toPredicateId(), pageable).map { it.toStatement() }

    override fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjectAndPredicate(subjectId, predicateId.toPredicateId(), pageable).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabel(
        predicateId: ThingId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabel(predicateId.toPredicateId(), literal, pageable).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId.toPredicateId(), literal, subjectClass, pageable)
            .map { it.toStatement() }

    override fun findAllBySubjects(subjectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjects(subjectIds, pageable).map { it.toStatement() }

    override fun findAllByObjects(objectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObjects(objectIds, pageable).map { it.toStatement() }

    override fun fetchAsBundle(id: ThingId, configuration: BundleConfiguration): Iterable<GeneralStatement> =
        neo4jRepository.fetchAsBundle(id, configuration.toApocConfiguration()).map { it.toStatement() }

    override fun exists(id: StatementId): Boolean = neo4jRepository.existsByStatementId(id)

    override fun countPredicateUsage(pageable: Pageable): Page<RetrieveStatementUseCase.PredicateUsageCount> =
        neo4jRepository.countPredicateUsage(pageable).map {
            RetrieveStatementUseCase.PredicateUsageCount(ThingId(it.id), it.count)
        }

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> =
        neo4jRepository.findDOIByContributionId(id.toResourceId()).map(Neo4jLiteral::toLiteral)

    override fun countPredicateUsage(id: ThingId) = neo4jRepository.countPredicateUsage(id.toPredicateId())

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jRepository.findByDOI(doi).map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findProblemsByObservatoryId(id, pageable).map(Neo4jResource::toResource)

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        neo4jRepository.findAllContributorsByResourceId(id.toResourceId(), pageable)

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        neo4jRepository.findTimelineByResourceId(id.toResourceId(), pageable)

    override fun checkIfResourceHasStatements(id: ThingId): Boolean =
        neo4jRepository.checkIfResourceHasStatements(id.toResourceId())

    override fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findProblemsByOrganizationId(id, pageable).map(Neo4jResource::toResource)

    override fun findBySubjectIdAndPredicateIdAndObjectId(
        subjectId: ThingId,
        predicateId: ThingId,
        objectId: ThingId
    ): Optional<GeneralStatement> =
        neo4jRepository.findBySubjectIdAndPredicateIdAndObjectId(subjectId, predicateId, objectId).map { it.toStatement() }

    private fun Neo4jStatement.toStatement(): GeneralStatement = GeneralStatement(
        id = statementId!!,
        subject = subject!!.toThing(),
        predicate = predicateRepository.findByPredicateId(ThingId(predicateId!!.value)).get(),
        `object` = `object`!!.toThing(),
        createdAt = createdAt!!,
        createdBy = createdBy
    )

    private fun Neo4jStatement.toStatement(lookupTable: PredicateLookupTable): GeneralStatement = GeneralStatement(
        id = statementId!!,
        subject = subject!!.toThing(),
        predicate = lookupTable[ThingId(predicateId!!.value)]
            ?: throw IllegalStateException("Predicate $predicateId not found in lookup table. This is a bug."),
        `object` = `object`!!.toThing(),
        createdAt = createdAt!!,
        createdBy = createdBy
    )

    private fun GeneralStatement.toNeo4jStatement(): Neo4jStatement =
        // Need to fetch the internal ID of a (possibly) existing entity to prevent creating a new one.
        neo4jRepository.findByStatementId(id!!).orElse(Neo4jStatement()).apply {
            statementId = this@toNeo4jStatement.id
            subject = this@toNeo4jStatement.subject.toNeo4jThing()
            `object` = this@toNeo4jStatement.`object`.toNeo4jThing()
            predicateId = this@toNeo4jStatement.predicate.id.toPredicateId()
            createdBy = this@toNeo4jStatement.createdBy
            createdAt = this@toNeo4jStatement.createdAt
        }

    private fun Thing.toNeo4jThing(): Neo4jThing =
        // The purpose of this method is of technical nature, as the OGM requires a reference to the start and end node.
        // With direct access to the database, this becomes obsolete, because queries can use the ID directly.
        when (this) {
            is Class -> neo4jClassRepository.findByClassId(this.id.toClassId()).get()
            is Literal -> neo4jLiteralRepository.findByLiteralId(this.id.toLiteralId()).get()
            is Predicate -> neo4jPredicateRepository.findByPredicateId(this.id.toPredicateId()).get()
            is Resource -> neo4jResourceRepository.findByResourceId(this.id.toResourceId()).get()
        }

    private fun BundleConfiguration.toApocConfiguration(): Map<String, Any> {
        val conf = mutableMapOf<String, Any>(
            "relationshipFilter" to ">",
            "bfs" to true
        )
        // configure min and max levels
        if (maxLevel != null)
            conf["maxLevel"] = maxLevel!!
        if (minLevel != null)
            conf["minLevel"] = minLevel!!
        // configure blacklisting and whitelisting classes
        var labelFilter = ""
        if (blacklist.isNotEmpty())
            labelFilter = blacklist.joinToString(prefix = "-", separator = "|-")
        if (whitelist.isNotEmpty()) {
            var positiveLabels = whitelist.joinToString(prefix = "+", separator = "|+")
            if (labelFilter.isNotBlank())
                positiveLabels += "|$labelFilter"
            labelFilter = positiveLabels
        }
        if (blacklist.isNotEmpty() || whitelist.isNotEmpty())
            conf["labelFilter"] = labelFilter
        return conf
    }
}
