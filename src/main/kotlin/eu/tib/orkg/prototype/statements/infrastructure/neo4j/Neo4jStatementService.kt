package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.BundleConfiguration
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.ClassId
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
import java.util.Optional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
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

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination).map { toStatement(it) }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByPredicateId(predicateId, pagination)
            .map { toStatement(it) }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)
            .map { toStatement(it) }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository
            .findAllBySubjectAndPredicate(subjectId, predicateId, pagination)
            .map { toStatement(it) }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository
            .findAllByObjectAndPredicate(objectId, predicateId, pagination)
            .map { toStatement(it) }

    override fun create(subject: String, predicate: PredicateId, `object`: String) =
        create(ContributorId.createUnknownContributor(), subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
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

        var id = neo4jStatementIdGenerator.nextIdentity()

        // Should be moved to the Generator in the future
        while (statementRepository.findByStatementId(id).isPresent) {
            id = neo4jStatementIdGenerator.nextIdentity()
        }

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

    override fun add(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String) {
        // This method mostly exists for performance reasons. We just create the statement but do not return anything.
        // That saves the extra calls to the database to retrieve the statement again, even if it may not be needed.

        val foundSubject = thingRepository
            .findByThingId(subject)
            .orElseThrow { IllegalStateException("Could not find subject $subject") }

        predicateService.findById(predicate)
            .orElseThrow { IllegalArgumentException("Predicate could not be found: $predicate") }

        val foundObject = thingRepository
            .findByThingId(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        val id = neo4jStatementIdGenerator.nextIdentity()

        statementRepository.save(
            Neo4jStatement(
                statementId = id,
                predicateId = predicate,
                subject = foundSubject,
                `object` = foundObject,
                createdBy = userId
            )
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

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination)
            .map { toStatement(it) }

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId, literal, subjectClass, pagination)
            .map { toStatement(it) }

    override fun fetchAsBundle(
        thingId: String,
        configuration: BundleConfiguration
    ): Bundle =
        Bundle(
            thingId,
            statementRepository.fetchAsBundle(
                thingId,
                configuration.toApocConfiguration()
            )
                .map { toStatement(it) }
                .toMutableList()
        )

    override fun removeAll() = statementRepository.deleteAll()

    private fun refreshObject(thing: Neo4jThing): Thing {
        return when (thing) {
            is Neo4jResource -> resourceService.findById(thing.resourceId).get()
            is Neo4jLiteral -> literalService.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }

    private fun toStatement(statement: Neo4jStatement) =
        GeneralStatement(
            id = statement.statementId!!,
            subject = refreshObject(statement.subject!!),
            predicate = predicateService.findById(statement.predicateId!!).get(),
            `object` = refreshObject(statement.`object`!!),
            createdAt = statement.createdAt!!,
            createdBy = statement.createdBy
        )
}
