package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResource
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementWithResourceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Service
@Transactional
class Neo4jStatementWithResourceService : StatementWithResourceService {

    @Autowired
    private lateinit var neo4jResourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var neo4jStatementRepository: Neo4jStatementWithResourceRepository

    @Autowired
    private lateinit var neo4jStatementIdGenerator: Neo4jStatementIdGenerator

    override fun create(subject: ResourceId, predicate: PredicateId, `object`: ResourceId) =
        create(UUID(0, 0), subject, predicate, `object`)

    override fun create(
        userId: UUID,
        subject: ResourceId,
        predicate: PredicateId,
        `object`: ResourceId
    ): StatementWithResource {
        val foundSubject = neo4jResourceRepository
            .findByResourceId(subject)
            .orElseThrow { IllegalStateException("Could not find subject") }

        val foundPredicate = predicateService.findById(predicate)
        if (!foundPredicate.isPresent)
            throw IllegalArgumentException("Resource could not be found.")

        val foundObject = neo4jResourceRepository
            .findByResourceId(`object`)
            .orElseThrow { IllegalStateException("Could not find object") }

        val id = neo4jStatementIdGenerator.nextIdentity()

        val persistedStatement = neo4jStatementRepository.save(
            Neo4jStatementWithResource(
                statementId = id,
                predicateId = predicate,
                subject = foundSubject,
                `object` = foundObject,
                createdBy = userId
            )
        )

        return StatementWithResource(
            persistedStatement.statementId!!,
            foundSubject.toResource(),
            foundPredicate.get(),
            foundObject.toObject(),
            persistedStatement.createdAt!!,
            createdBy = persistedStatement.createdBy
        )
    }

    override fun findAll(pagination: Pageable): Iterable<StatementWithResource> {
        val statements = neo4jStatementRepository.findAll(pagination).content
        val ids = distinctIdsOf(statements)
        val counts = ids.zip(neo4jResourceRepository.getIncomingStatementsCount(ids)).toMap()
        return statements.map { toStatement(it, counts[it.`object`!!.resourceId]!!.toInt()) }
    }

    override fun findById(statementId: StatementId): Optional<StatementWithResource> {
        return neo4jStatementRepository
            .findByStatementId(statementId)
            .map {
                val newObject = neo4jResourceRepository.findByResourceId(it.`object`!!.resourceId).get()
                toStatement(it, newObject)
            }
    }

    override fun findAllBySubject(resourceId: ResourceId, pagination: Pageable): Iterable<StatementWithResource> {
        val resource = neo4jResourceRepository.findByResourceId(resourceId).get()
        val statements = neo4jStatementRepository.findAllBySubject(resource.resourceId!!, pagination).content
        val ids = distinctIdsOf(statements)
        val counts = ids.zip(neo4jResourceRepository.getIncomingStatementsCount(ids)).toMap()
        return statements.map { toStatement(it, counts[it.`object`!!.resourceId]!!.toInt()) }
    }

    override fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Iterable<StatementWithResource> {
        val statements =
            neo4jStatementRepository.findAllBySubjectAndPredicate(resourceId, predicateId, pagination).content
        val ids = distinctIdsOf(statements)
        val counts = ids.zip(neo4jResourceRepository.getIncomingStatementsCount(ids)).toMap()
        return statements.map { toStatement(it, counts[it.`object`!!.resourceId]!!.toInt()) }
    }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<StatementWithResource> {
        val statements = neo4jStatementRepository.findAllByPredicateId(predicateId, pagination).content
        val ids = distinctIdsOf(statements)
        val counts = ids.zip(neo4jResourceRepository.getIncomingStatementsCount(ids)).toMap()
        return statements.map { toStatement(it, counts[it.`object`!!.resourceId]!!.toInt()) }
    }

    override fun findAllByObject(objectId: ResourceId, pagination: Pageable): Iterable<StatementWithResource> {
        val resource = neo4jResourceRepository.findByResourceId(objectId)
        if (resource.isPresent) {
            return neo4jStatementRepository
                .findAllByObject(resource.get().resourceId!!, pagination)
                .content
                .map { toStatement(it) }
        }
        return emptyList()
    }

    override fun totalNumberOfStatements() = neo4jStatementRepository.count()

    override fun remove(statementId: StatementId) {
        val toDelete = neo4jStatementRepository.findByStatementId(statementId)

        neo4jStatementRepository.delete(toDelete.get())
    }

    override fun update(statementEditRequest: StatementEditRequest): StatementWithResource {
        // already checked by service
        val found = neo4jStatementRepository.findByStatementId(statementEditRequest.statementId!!).get()

        // update all the properties
        found.predicateId = statementEditRequest.predicateId
        found.subject = neo4jResourceRepository.findByResourceId(statementEditRequest.subjectId).get()
        found.`object` = neo4jResourceRepository.findByResourceId(statementEditRequest.objectId).get()

        neo4jStatementRepository.save(found)

        return toStatement(found)
    }

    private fun distinctIdsOf(statements: MutableList<Neo4jStatementWithResource>) =
        statements.distinctBy { it.`object`!!.resourceId!! }.map { it.`object`!!.resourceId!! }

    private fun toStatement(
        statement: Neo4jStatementWithResource,
        `object`: Neo4jResource,
        shared: Int = 0
    ) =
        StatementWithResource(
            id = statement.statementId!!,
            subject = statement.subject!!.toResource(),
            predicate = predicateService.findById(statement.predicateId!!).get(),
            `object` = `object`.toObject(shared),
            createdAt = statement.createdAt!!,
            createdBy = statement.createdBy
        )

    private fun toStatement(statement: Neo4jStatementWithResource) =
        toStatement(statement, statement.`object`!!)

    private fun toStatement(statement: Neo4jStatementWithResource, shared: Int) =
        toStatement(statement, statement.`object`!!, shared)

    override fun countStatements(paperId: ResourceId?): Int =
        neo4jStatementRepository.countByIdRecursive(paperId)
}
