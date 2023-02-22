package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.application.StatementNotFound
import eu.tib.orkg.prototype.statements.application.StatementObjectNotFound
import eu.tib.orkg.prototype.statements.application.StatementPredicateNotFound
import eu.tib.orkg.prototype.statements.application.StatementSubjectNotFound
import eu.tib.orkg.prototype.statements.domain.model.Bundle
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
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
    private val templateRepository: TemplateRepository,
    private val flags: FeatureFlagService
) : StatementUseCases {

    override fun findAll(pagination: Pageable): Iterable<StatementRepresentation> =
        retrieveAndConvertIterable { statementRepository.findAll(pagination).content }

    override fun findById(statementId: StatementId): Optional<StatementRepresentation> =
        retrieveAndConvertSingleStatement { statementRepository.findByStatementId(statementId) }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllBySubject(subjectId, pagination) }

    override fun findAllByPredicate(predicateId: ThingId, pagination: Pageable): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByPredicateId(predicateId, pagination) }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByObject(objectId, pagination) }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pagination) }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByObjectAndPredicate(objectId, predicateId, pagination) }

    @Transactional(readOnly = true)
    override fun exists(id: StatementId): Boolean = statementRepository.exists(id)

    override fun create(subject: String, predicate: ThingId, `object`: String): StatementRepresentation =
        create(ContributorId.createUnknownContributor(), subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
        subject: String,
        predicate: ThingId,
        `object`: String
    ): StatementRepresentation {
        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { StatementSubjectNotFound(subject) }

        val foundPredicate = predicateService.findByPredicateId(predicate)
            .orElseThrow { StatementPredicateNotFound(predicate) }

        val foundObject = thingRepository.findByThingId(`object`)
            .orElseThrow { StatementObjectNotFound(`object`) }

        val id = statementRepository.nextIdentity()
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

    override fun add(userId: ContributorId, subject: String, predicate: ThingId, `object`: String) {
        // This method mostly exists for performance reasons. We just create the statement but do not return anything.
        // That saves the extra calls to the database to retrieve the statement again, even if it may not be needed.

        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { StatementSubjectNotFound(subject) }

        val foundPredicate = predicateService.findByPredicateId(predicate)
            .orElseThrow { StatementPredicateNotFound(predicate) }

        val foundObject = thingRepository.findByThingId(`object`)
            .orElseThrow { StatementObjectNotFound(`object`) }

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
        if (statementRepository.exists(statementId)) {
            statementRepository.deleteByStatementId(statementId)
        }
    }

    override fun update(statementEditRequest: StatementEditRequest): StatementRepresentation {
        var found = statementRepository.findByStatementId(statementEditRequest.statementId!!)
            .orElseThrow { StatementNotFound(statementEditRequest.statementId.value) }

        statementEditRequest.subjectId?.let {
            @Suppress("UNUSED_VARIABLE") // It is unused, because commented out. This method needs a re-write.
            val foundSubject = thingRepository.findByThingId(statementEditRequest.subjectId)
                .orElseThrow { StatementSubjectNotFound(statementEditRequest.subjectId) }
            // found = found.copy(subject = foundSubject) // TODO: does this make sense?
        }

        statementEditRequest.predicateId?.let {
            val foundPredicate = predicateService.findByPredicateId(statementEditRequest.predicateId)
                .orElseThrow { StatementPredicateNotFound(statementEditRequest.predicateId) }
            found = found.copy(predicate = foundPredicate)
        }

        statementEditRequest.objectId?.let {
            val foundObject = thingRepository.findByThingId(statementEditRequest.objectId)
                .orElseThrow { StatementObjectNotFound(statementEditRequest.objectId) }
            found = found.copy(`object` = foundObject)
        }

        statementRepository.save(found)

        return findById(found.id!!).get()
    }

    override fun countStatements(paperId: String): Long = statementRepository.countByIdRecursive(paperId)

    override fun findAllByPredicateAndLabel(
        predicateId: ThingId,
        literal: String,
        pagination: Pageable
    ): Page<StatementRepresentation> =
        retrieveAndConvertPaged { statementRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination) }

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
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

    override fun countPredicateUsage(pageable: Pageable) =
        statementRepository.countPredicateUsage(pageable)

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
            statementRepository.fetchAsBundle(thingId, configuration)
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
            statementRepository.fetchAsBundle(thingId, BundleConfiguration.firstLevelConf())
        }.toMutableList()
    )

    override fun removeAll() = statementRepository.deleteAll()

    private fun countsFor(statements: List<GeneralStatement>): Map<ResourceId, Long> {
        val resourceIds =
            statements.map(GeneralStatement::subject).filterIsInstance<Resource>().mapNotNull { it.id } +
                statements.map(GeneralStatement::`object`).filterIsInstance<Resource>().mapNotNull { it.id }
        return statementRepository.countStatementsAboutResources(resourceIds.toSet())
    }

    private fun formatLabelFor(statements: List<GeneralStatement>): Map<ResourceId, FormattedLabel?> =
        if (flags.isFormattedLabelsEnabled()) {
            (statements.map(GeneralStatement::subject).filterIsInstance<Resource>() +
                statements.map(GeneralStatement::`object`).filterIsInstance<Resource>())
                .associate { it.id!! to templateRepository.formattedLabelFor(it.id, it.classes) }
        } else emptyMap()

    private fun retrieveAndConvertSingleStatement(action: () -> Optional<GeneralStatement>): Optional<StatementRepresentation> {
        val instance = action()
        return instance.map {
            val counts = countsFor(listOf(instance.get()))
            val labels = formatLabelFor(listOf(instance.get()))
            it.toRepresentation(counts, labels)
        }
    }

    private fun retrieveAndConvertPaged(action: () -> Page<GeneralStatement>): Page<StatementRepresentation> {
        val paged = action()
        val statementCounts = countsFor(paged.content)
        val formattedLabelCounts = formatLabelFor(paged.content)
        return paged.map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    private fun retrieveAndConvertIterable(action: () -> Iterable<GeneralStatement>): Iterable<StatementRepresentation> {
        val statements = action()
        val statementCounts = countsFor(statements.toList())
        val formattedLabelCounts = formatLabelFor(statements.toList())
        return statements.map { it.toRepresentation(statementCounts, formattedLabelCounts) }
    }

    private fun GeneralStatement.toRepresentation(statementCounts: StatementCounts, formattedLabels: FormattedLabels): StatementRepresentation =
        object : StatementRepresentation {
            override val id: StatementId = this@toRepresentation.id!!
            override val subject: ThingRepresentation = this@toRepresentation.subject.toRepresentation(statementCounts, formattedLabels)
            override val predicate: PredicateRepresentation =
                this@toRepresentation.predicate.toPredicateRepresentation()
            override val `object`: ThingRepresentation =
                this@toRepresentation.`object`.toRepresentation(statementCounts, formattedLabels)
            override val createdAt: OffsetDateTime = this@toRepresentation.createdAt!!
            override val createdBy: ContributorId = this@toRepresentation.createdBy
        }
}

fun Thing.toRepresentation(usageCount: StatementCounts, formattedLabels: FormattedLabels): ThingRepresentation =
    when (this) {
        is Class -> toClassRepresentation()
        is Literal -> toLiteralRepresentation()
        is Predicate -> toPredicateRepresentation()
        is Resource -> toResourceRepresentation(usageCount, formattedLabels)
    }
