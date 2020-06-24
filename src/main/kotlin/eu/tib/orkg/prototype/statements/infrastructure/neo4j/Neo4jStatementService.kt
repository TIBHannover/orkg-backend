package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
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
import java.util.Optional
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
            .map { toStatement(it) }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)
            .map { toStatement(it) }

    override fun findAllBySubject(subjectId: String, pagination: Pageable, formatted: Boolean): Iterable<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination)
            .content
            .map { toStatement(it, formatted) }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Iterable<GeneralStatement> =
        this.findAllBySubject(subjectId, pagination, true)

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAllByPredicateId(predicateId, pagination)
            .content
            .map { toStatement(it) }

    override fun findAllByObject(objectId: String, pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)
            .content
            .map { toStatement(it) }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ) =
        statementRepository
            .findAllBySubjectAndPredicate(subjectId, predicateId, pagination)
            .content
            .map { toStatement(it) }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ) =
        statementRepository
            .findAllByObjectAndPredicate(objectId, predicateId, pagination)
            .content
            .map { toStatement(it) }

    override fun create(subject: String, predicate: PredicateId, `object`: String) =
        create(UUID(0, 0), subject, predicate, `object`)

    override fun create(
        userId: UUID,
        subject: String,
        predicate: PredicateId,
        `object`: String
    ): GeneralStatement {
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
                `object` = foundObject,
                createdBy = userId
            )
        )

        return GeneralStatement(
            persistedStatement.statementId!!,
            foundSubject.toThing(),
            foundPredicate.get(),
            foundObject.toThing(),
            persistedStatement.createdAt!!,
            persistedStatement.createdBy
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

        return toStatement(found)
    }

    override fun countStatements(paperId: String): Int =
        statementRepository.countByIdRecursive(paperId)

    override fun findTemplate(classId: ClassId): Optional<Resource> =
        statementRepository.findTemplate(classId)
            .map(Neo4jResource::toResource)

    override fun checkIfTemplateIsFormatted(templateId: ResourceId): Optional<Literal> =
        statementRepository.checkIfTemplateIsFormatted(templateId)
            .map(Neo4jLiteral::toLiteral)

    private fun refreshObject(thing: Neo4jThing, formatted: Boolean = true): Thing {
        return when (thing) {
            is Neo4jResource -> resourceService.findById(thing.resourceId, formatted).get()
            is Neo4jLiteral -> literalService.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }

    private fun toStatement(statement: Neo4jStatement, formatted: Boolean = true) =
        GeneralStatement(
            id = statement.statementId!!,
            subject = refreshObject(statement.subject!!, formatted),
            predicate = predicateService.findById(statement.predicateId!!).get(),
            `object` = refreshObject(statement.`object`!!, formatted),
            createdAt = statement.createdAt!!,
            createdBy = statement.createdBy
        )
}