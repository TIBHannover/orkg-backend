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
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.OwnershipInfo
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

typealias PredicateLookupTable = Map<ThingId, Predicate>

@Component
class SpringDataNeo4jStatementAdapter(
    val neo4jRepository: Neo4jStatementRepository,
    val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    val neo4jResourceRepository: Neo4jResourceRepository,
    val neo4jPredicateRepository: Neo4jPredicateRepository,
    val neo4jLiteralRepository: Neo4jLiteralRepository,
    val neo4jClassRepository: Neo4jClassRepository,
    private val cacheManager: CacheManager
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
        deleteByStatementId(statement.id!!)
    }

    override fun deleteByStatementId(id: StatementId) {
        neo4jRepository.deleteByStatementId(id).ifPresent {
            cacheManager.getCache(LITERAL_ID_TO_LITERAL_CACHE)?.evictIfPresent(it)
            cacheManager.getCache(LITERAL_ID_TO_LITERAL_EXISTS_CACHE)?.evictIfPresent(it)
            cacheManager.getCache(THING_ID_TO_THING_CACHE)?.evictIfPresent(it)
        }
    }

    override fun deleteByStatementIds(ids: Set<StatementId>) {
        // Fix OGM interpreting a singleton list as a string value of the first element
        if (ids.size == 1) {
            deleteByStatementId(ids.single())
        } else if (ids.size > 1) {
            val literals = neo4jRepository.deleteByStatementIds(ids)
            if (literals.isNotEmpty()) {
                val literalCache = cacheManager.getCache(LITERAL_ID_TO_LITERAL_CACHE)!!
                val literalExistsCache = cacheManager.getCache(LITERAL_ID_TO_LITERAL_EXISTS_CACHE)!!
                val thingCache = cacheManager.getCache(THING_ID_TO_THING_CACHE)!!
                literals.forEach {
                    literalCache.evictIfPresent(it)
                    literalExistsCache.evictIfPresent(it)
                    thingCache.evictIfPresent(it)
                }
            }
        }
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> {
        val neo4jStatements = neo4jRepository.findAll(pageable)
        val predicateIds = neo4jStatements.content.mapNotNull(Neo4jStatement::predicateId).toSet()
        val table = neo4jPredicateRepository.findAllByIdIn(predicateIds)
            .map(Neo4jPredicate::toPredicate)
            .associateBy { it.id }
        return neo4jStatements.map { it.toStatement(table) }
    }

    override fun countStatementsAboutResource(id: ThingId): Long =
        neo4jRepository.countStatementsByObjectId(id)

    override fun countStatementsAboutResources(resourceIds: Set<ThingId>): Map<ThingId, Long> =
        neo4jRepository.countStatementsAboutResource(resourceIds).associate { ThingId(it.id) to it.count }

    override fun determineOwnership(statementIds: Set<StatementId>): Set<OwnershipInfo> {
        // Fix OGM interpreting a singleton list as a string value of the first element
        return if (statementIds.size == 1) {
            neo4jRepository.findByStatementId(statementIds.single())
                .map { setOf(OwnershipInfo(it.statementId!!, it.createdBy)) }
                .orElseGet { emptySet() }
        } else {
            neo4jRepository.findAllByStatementIdIn(statementIds)
                .map { OwnershipInfo(it.statementId!!, it.createdBy) }
                .toSet()
        }
    }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> =
        neo4jRepository.findByStatementId(id).map { it.toStatement() }

    override fun findAllBySubject(subjectId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllBySubject(subjectId, pageable).map { it.toStatement() }

    override fun findAllByPredicateId(predicateId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateId(predicateId, pageable).map { it.toStatement() }

    override fun findAllByObject(objectId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        neo4jRepository.findAllByObject(objectId, pageable).map { it.toStatement() }

    override fun countByIdRecursive(id: ThingId): Long = neo4jRepository.countByIdRecursive(id)

    override fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByObjectAndPredicate(objectId, predicateId, pageable).map { it.toStatement() }

    override fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pageable).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabel(
        predicateId: ThingId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabel(predicateId, literal, pageable).map { it.toStatement() }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        neo4jRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId, literal, subjectClass, pageable)
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
        neo4jRepository.findDOIByContributionId(id).map(Neo4jLiteral::toLiteral)

    override fun countPredicateUsage(id: ThingId) = neo4jRepository.countPredicateUsage(id)

    override fun findByDOI(doi: String): Optional<Resource> =
        neo4jRepository.findByDOI(doi).map(Neo4jResource::toResource)

    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findProblemsByObservatoryId(id, pageable).map(Neo4jResource::toResource)

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        neo4jRepository.findAllContributorsByResourceId(id, pageable)

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        neo4jRepository.findTimelineByResourceId(id, pageable)

    override fun checkIfResourceHasStatements(id: ThingId): Boolean =
        neo4jRepository.checkIfResourceHasStatements(id)

    override fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllProblemsByOrganizationId(id, pageable).map(Neo4jResource::toResource)

    override fun findBySubjectIdAndPredicateIdAndObjectId(
        subjectId: ThingId,
        predicateId: ThingId,
        objectId: ThingId
    ): Optional<GeneralStatement> =
        neo4jRepository.findBySubjectIdAndPredicateIdAndObjectId(subjectId, predicateId, objectId).map { it.toStatement() }

    private fun Neo4jStatement.toStatement() = toStatement(neo4jPredicateRepository)

    private fun GeneralStatement.toNeo4jStatement() = toNeo4jStatement(
        neo4jStatementRepository = neo4jRepository,
        neo4jClassRepository = neo4jClassRepository,
        neo4jLiteralRepository = neo4jLiteralRepository,
        neo4jPredicateRepository = neo4jPredicateRepository,
        neo4jResourceRepository = neo4jResourceRepository
    )

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
