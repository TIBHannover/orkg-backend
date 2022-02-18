package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.BundleConfiguration
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StatementService(
    private val thingRepository: ThingRepository,
    private val resourceService: ResourceRepository,
    private val literalService: LiteralRepository,
    private val predicateService: PredicateRepository,
    private val statementRepository: StatementRepository,
) : StatementUseCases {

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> =
        statementRepository.findAll(pagination).content

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination)

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByPredicateId(predicateId, pagination)

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> = statementRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pagination)

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> = statementRepository.findAllByObjectAndPredicate(objectId, predicateId, pagination)

    override fun create(subject: String, predicate: PredicateId, `object`: String) =
        create(ContributorId.createUnknownContributor(), subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
        subject: String,
        predicate: PredicateId,
        `object`: String
    ): GeneralStatement {
        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { IllegalStateException("Could not find subject $subject") }

        val foundPredicate = predicateService.findByPredicateId(predicate)
            .orElseThrow { IllegalArgumentException("predicate could not be found: $predicate") }

        val foundObject = thingRepository.findByThingId(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        var id = statementRepository.nextIdentity()

        // Should be moved to the Generator in the future
        while (statementRepository.findByStatementId(id).isPresent) {
            id = statementRepository.nextIdentity()
        }

        val newStatement = GeneralStatement(
            id = id,
            subject = foundSubject,
            predicate = foundPredicate,
            `object` = foundObject,
            createdBy = userId,
            createdAt = OffsetDateTime.now(),
        )
        statementRepository.save(newStatement)
        return newStatement
    }

    override fun add(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String) {
        // This method mostly exists for performance reasons. We just create the statement but do not return anything.
        // That saves the extra calls to the database to retrieve the statement again, even if it may not be needed.

        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { IllegalStateException("Could not find subject $subject") }

        val foundPredicate = predicateService.findByPredicateId(predicate)
            .orElseThrow { IllegalArgumentException("Predicate could not be found: $predicate") }

        val foundObject = thingRepository.findByThingId(`object`)
            .orElseThrow { IllegalStateException("Could not find object: $`object`") }

        val id = statementRepository.nextIdentity()

        val statement = GeneralStatement(
            id = id,
            predicate = foundPredicate,
            subject = foundSubject,
            `object` = foundObject,
            createdBy = userId,
            createdAt = OffsetDateTime.now(),
        )
        statementRepository.save(statement)
    }

    override fun totalNumberOfStatements(): Long = statementRepository.count()

    override fun remove(statementId: StatementId) {
        val toDelete = statementRepository.findByStatementId(statementId)
        statementRepository.delete(toDelete.get())
    }

    override fun update(statementEditRequest: StatementEditRequest): GeneralStatement {
        var found = statementRepository.findByStatementId(statementEditRequest.statementId!!)
            .orElseThrow { IllegalStateException("Could not find statement: ${statementEditRequest.statementId}") }

        val foundSubject = thingRepository.findByThingId(statementEditRequest.subjectId)
            .orElseThrow { IllegalStateException("Could not find subject ${statementEditRequest.subjectId}") }

        val foundPredicate = predicateService.findByPredicateId(statementEditRequest.predicateId)
            .orElseThrow { IllegalArgumentException("Predicate could not be found: ${statementEditRequest.predicateId}") }

        val foundObject = thingRepository.findByThingId(statementEditRequest.objectId)
            .orElseThrow { IllegalStateException("Could not find object: ${statementEditRequest.objectId}") }

        // update all the properties
        found = found.copy(predicate = foundPredicate)
        // found = found.copy(subject = foundSubject) // TODO: does this make sense?
        found = found.copy(`object` = foundObject)

        statementRepository.save(found)

        return found
    }

    override fun countStatements(paperId: String): Int = statementRepository.countByIdRecursive(paperId)

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> = statementRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination)

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(predicateId, literal, subjectClass, pagination)

    override fun fetchAsBundle(
        thingId: String,
        configuration: BundleConfiguration,
        includeFirst: Boolean
    ): Bundle {
        return when (includeFirst) {
            true -> createBundleFirstIncluded(thingId, configuration)
            false -> createBundle(thingId, configuration)
        }
    }

    /**
     * Create a bundle where the first level is not included in the statements.
     *
     * @param thingId the root thing to start from.
     * @param configuration the bundle configuration passed down.
     *
     * @return returns a Bundle object
     */
    private fun createBundle(
        thingId: String,
        configuration: BundleConfiguration
    ): Bundle = Bundle(
        thingId, statementRepository.fetchAsBundle(
            thingId, configuration.toApocConfiguration()
        ).toMutableList()
    )

    /**
     * Create a bundle including the first level in the statements.
     * NOTE: this function calls createBundle internally!
     *
     * @param thingId the root thing to start from.
     * @param configuration the bundle configuration passed down.
     *
     * @return returns a Bundle object (addition of normal bundle and first level bundle)
     */
    private fun createBundleFirstIncluded(
        thingId: String,
        configuration: BundleConfiguration
    ): Bundle = createBundle(thingId, configuration) + Bundle(
        thingId, statementRepository.fetchAsBundle(
            thingId, BundleConfiguration.firstLevelConf().toApocConfiguration()
        ).toMutableList()
    )

    override fun removeAll() = statementRepository.deleteAll()
}
