package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.BundleConfiguration
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.ports.PredicateRepository
import eu.tib.orkg.prototype.statements.ports.StatementRepository
import eu.tib.orkg.prototype.statements.ports.ThingRepository
import java.time.OffsetDateTime
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jStatementService(
    private val thingRepository: ThingRepository,
    private val predicateRepository: PredicateRepository,
    private val statementRepository: StatementRepository
) :
    StatementService {

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAll(pagination)

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findById(statementId)

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination)

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByPredicate(predicateId, pagination)

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository
            .findAllBySubjectAndPredicate(subjectId, predicateId, pagination)

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository
            .findAllByObjectAndPredicate(objectId, predicateId, pagination)

    override fun create(subject: String, predicate: PredicateId, `object`: String) =
        create(ContributorId.createUnknownContributor(), subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
        subject: String,
        predicate: PredicateId,
        `object`: String
    ): GeneralStatement {
        val foundSubject = thingRepository
            .findById(subject)
            .orElseThrow { IllegalStateException("Could not find subject $subject") }

        val foundPredicate = predicateRepository.findById(predicate)
            .orElseThrow { IllegalArgumentException("predicate could not be found: $predicate") }

        val foundObject = thingRepository
            .findById(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        var id = statementRepository.nextIdentity()

        // Should be moved to the Generator in the future
        while (statementRepository.findById(id).isPresent) {
            id = statementRepository.nextIdentity()
        }

        val statement = GeneralStatement(
            id = id,
            predicate = foundPredicate,
            subject = foundSubject,
            `object` = foundObject,
            createdBy = userId,
            createdAt = OffsetDateTime.now()
        )
        statementRepository.save(statement)
        return statement
    }

    override fun add(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String) {
        // This method mostly exists for performance reasons. We just create the statement but do not return anything.
        // That saves the extra calls to the database to retrieve the statement again, even if it may not be needed.

        val foundSubject = thingRepository
            .findById(subject)
            .orElseThrow { IllegalStateException("Could not find subject $subject") }

        val foundPredicate = predicateRepository.findById(predicate)
            .orElseThrow { IllegalArgumentException("Predicate could not be found: $predicate") }

        val foundObject = thingRepository
            .findById(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        val id = statementRepository.nextIdentity()

        statementRepository.save(
            GeneralStatement(
                id = id,
                predicate = foundPredicate,
                subject = foundSubject,
                `object` = foundObject,
                createdBy = userId,
                createdAt = OffsetDateTime.now()
            )
        )
    }

    override fun totalNumberOfStatements(): Long =
        statementRepository.count()

    override fun remove(statementId: StatementId) {
        val toDelete = statementRepository.findById(statementId)
        statementRepository.delete(toDelete.get())
    }

    override fun update(statementEditRequest: StatementEditRequest): GeneralStatement {
        // FIXME: This is very, very broken and needs a rewrite. Clients depend on it, so we need to define semantics around it.
        val found = statementRepository.findById(statementEditRequest.statementId!!)
        if (found.isPresent) {
            val new = found.get()
                .copy(predicate = predicateRepository.findById(statementEditRequest.predicateId).get())
                .copy(subject = thingRepository.findById(statementEditRequest.subjectId).get())
                .copy(`object` = thingRepository.findById(statementEditRequest.objectId).get())
            statementRepository.save(new)
            return new
        }
        return found.get()
    }

    override fun countStatements(paperId: String): Long =
        statementRepository.countByIdRecursive(paperId)

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateAndLabel(predicateId, literal, pagination)

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateAndLabelAndSubjectClass(predicateId, literal, subjectClass, pagination)

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
                .toMutableList()
        )

    // FIXME: To be removed
    override fun removeAll() = Unit
}
