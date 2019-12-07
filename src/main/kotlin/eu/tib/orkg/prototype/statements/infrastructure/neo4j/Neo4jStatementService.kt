package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
@Transactional
class Neo4jStatementService :
    StatementService {

    @Autowired
    private lateinit var thingRepository: Neo4jThingRepository

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var literalService: LiteralService

    @Autowired
    private lateinit var statementRepository: Neo4jStatementRepository

    @Autowired
    private lateinit var neo4jStatementIdGenerator: Neo4jStatementIdGenerator

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAll(pagination)
            .content
            .map {
                GeneralStatement(
                    it.statementId,
                    refreshObject(it.subject!!),
                    predicateService.findById(it.predicateId).get(),
                    refreshObject(it.`object`!!),
                    it.createdAt
                )
            }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)
            .map {
                GeneralStatement(
                    it.statementId,
                    refreshObject(it.subject!!),
                    predicateService.findById(it.predicateId).get(),
                    refreshObject(it.`object`!!),
                    it.createdAt
                )
            }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination)
            .content
            .map {
                GeneralStatement(
                    it.statementId,
                    refreshObject(it.subject!!),
                    predicateService.findById(it.predicateId).get(),
                    refreshObject(it.`object`!!),
                    it.createdAt
                )
            }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAllByPredicateId(predicateId, pagination)
            .content
            .map {
                GeneralStatement(
                    it.statementId,
                    refreshObject(it.subject!!),
                    predicateService.findById(it.predicateId).get(),
                    refreshObject(it.`object`!!),
                    it.createdAt
                )
            }

    override fun findAllByObject(objectId: String, pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)
            .content
            .map {
                GeneralStatement(
                    it.statementId,
                    refreshObject(it.subject!!),
                    predicateService.findById(it.predicateId).get(),
                    refreshObject(it.`object`!!),
                    it.createdAt
                )
            }

    override fun create(subject: String, predicate: PredicateId, `object`: String): GeneralStatement {
        val foundSubject = thingRepository
            .findByThingId(subject)
            .orElseThrow { IllegalStateException("Could not find subject $subject") }

        val foundPredicate = predicateService.findById(predicate)
        if (!foundPredicate.isPresent)
            throw IllegalArgumentException("predicate could not be found: $predicate")

        val foundObject = thingRepository
            .findByThingId(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        val id = neo4jStatementIdGenerator.nextIdentity()

        val persistedStatement = statementRepository.save(
            Neo4jStatement(
                statementId = id,
                predicateId = predicate,
                subject = foundSubject,
                `object` = foundObject
            )
        )

        return GeneralStatement(
            persistedStatement.statementId!!,
            foundSubject.toThing(),
            foundPredicate.get(),
            foundObject.toThing(),
            persistedStatement.createdAt!!
        )
    }

    override fun totalNumberOfStatements(): Long =
        statementRepository.count()

    override fun remove(statementId: StatementId) {
        val toDelete = statementRepository.findByStatementId(statementId)
        statementRepository.delete(toDelete.get())
    }

    override fun update(statementEditRequest: StatementEditRequest): GeneralStatement {
        val found = statementRepository.findByStatementId(statementEditRequest.statementId!!).get()

        // update all the properties
        found.predicateId = statementEditRequest.predicateId
        found.subject = thingRepository.findByThingId(statementEditRequest.subjectId).get()
        found.`object` = thingRepository.findByThingId(statementEditRequest.objectId).get()

        statementRepository.save(found)

        return GeneralStatement(
            found.statementId!!,
            found.subject!!.toThing(),
            predicateService.findById(found.predicateId).get(),
            found.`object`!!.toThing(),
            found.createdAt!!
        )
    }

    private fun refreshObject(thing: Neo4jThing): Thing {
        return when (thing) {
            is Neo4jResource -> resourceService.findById(thing.resourceId).get()
            is Neo4jLiteral -> literalService.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }
}
