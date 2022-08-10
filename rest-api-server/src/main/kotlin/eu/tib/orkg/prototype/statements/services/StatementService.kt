package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.BundleConfiguration
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

typealias StatementCounts = Map<ResourceId, Long>

@Service
@Transactional
class StatementService(
    private val thingRepository: ThingRepository,
    private val predicateService: PredicateRepository,
    private val statementRepository: StatementRepository,
) : StatementUseCases {

    override fun findAll(pagination: Pageable): Iterable<StatementRepresentation> =
        retrieveAndConvertIterable { statementRepository.findAll(pagination).content }

    override fun findById(statementId: StatementId): Optional<StatementRepresentation> =
        retrieveAndConvertSingleStatement { statementRepository.findByStatementId(statementId) }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllBySubject(subjectId, pagination) }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByPredicateId(predicateId, pagination) }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByObject(objectId, pagination) }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pagination) }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByObjectAndPredicate(objectId, predicateId, pagination) }

    override fun create(subject: String, predicate: PredicateId, `object`: String): StatementRepresentation =
        create(ContributorId.createUnknownContributor(), subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
        subject: String,
        predicate: PredicateId,
        `object`: String
    ): StatementRepresentation {
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
        return findById(newStatement.id!!).get()
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

    override fun update(statementEditRequest: StatementEditRequest): StatementRepresentation {
        var found = statementRepository.findByStatementId(statementEditRequest.statementId!!)
            .orElseThrow { IllegalStateException("Could not find statement: ${statementEditRequest.statementId}") }

        @Suppress("UNUSED_VARIABLE") // It is unused, because commented out. This method needs a re-write.
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

        return findById(found.id!!).get()
    }

    override fun countStatements(paperId: String): Int = statementRepository.countByIdRecursive(paperId)

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination) }

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId,
                literal,
                subjectClass,
                pagination
            )
        }

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
        thingId, retrieveAndConvertIterable {
            statementRepository.fetchAsBundle(
                thingId, configuration.toApocConfiguration()
            )
        }.toMutableList()
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
        thingId, retrieveAndConvertIterable {
            statementRepository.fetchAsBundle(
                thingId, BundleConfiguration.firstLevelConf().toApocConfiguration()
            )
        }.toMutableList()
    )

    override fun removeAll() = statementRepository.deleteAll()

    private fun countsFor(statements: List<GeneralStatement>): Map<ResourceId, Long> {
        val resourceIds =
            statements.map(GeneralStatement::subject).filterIsInstance<Resource>().mapNotNull { it.id } +
                statements.map(GeneralStatement::`object`).filterIsInstance<Resource>().mapNotNull { it.id }
        return statementRepository.countStatementsAboutResources(resourceIds.toSet())
    }

    private fun retrieveAndConvertSingleStatement(action: () -> Optional<GeneralStatement>): Optional<StatementRepresentation> =
        action().map {
            val counts = countsFor(listOf(action().get(), action().get()))
            it.toRepresentation(counts)
        }

    private fun retrieveAndConvertPaged(action: () -> Page<GeneralStatement>): Page<StatementRepresentation> {
        val paged = action()
        return paged.map { it.toRepresentation(countsFor(paged.content)) }
    }

    private fun retrieveAndConvertIterable(action: () -> Iterable<GeneralStatement>): Iterable<StatementRepresentation> {
        val statements = action()
        return statements.map { it.toRepresentation(countsFor(statements.toList())) }
    }

    private fun GeneralStatement.toRepresentation(statementCounts: StatementCounts): StatementRepresentation =
        object : StatementRepresentation {
            override val id: StatementId = this@toRepresentation.id!!
            override val subject: ThingRepresentation = this@toRepresentation.subject.toRepresentation(statementCounts)
            override val predicate: PredicateRepresentation =
                this@toRepresentation.predicate.toPredicateRepresentation()
            override val `object`: ThingRepresentation =
                this@toRepresentation.`object`.toRepresentation(statementCounts)
            override val createdAt: OffsetDateTime = this@toRepresentation.createdAt!!
            override val createdBy: ContributorId = this@toRepresentation.createdBy
        }
}

fun Thing.toRepresentation(usageCount: StatementCounts): ThingRepresentation =
    when (this) {
        is Class -> toClassRepresentation()
        is Literal -> toLiteralRepresentation()
        is Predicate -> toPredicateRepresentation()
        is Resource -> toResourceRepresentation(usageCount)
    }
