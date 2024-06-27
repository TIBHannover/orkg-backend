package org.orkg.graph.domain

import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StatementService(
    private val thingRepository: ThingRepository,
    private val predicateService: PredicateRepository,
    private val statementRepository: StatementRepository,
    private val literalRepository: LiteralRepository,
    private val clock: Clock,
) : StatementUseCases {

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)

    @Transactional(readOnly = true)
    override fun exists(id: StatementId): Boolean = statementRepository.exists(id)

    override fun findAll(
        pageable: Pageable,
        subjectClasses: Set<ThingId>,
        subjectId: ThingId?,
        subjectLabel: String?,
        predicateId: ThingId?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        objectClasses: Set<ThingId>,
        objectId: ThingId?,
        objectLabel: String?
    ): Page<GeneralStatement> =
        statementRepository.findAll(
            pageable = pageable,
            subjectClasses = subjectClasses,
            subjectId = subjectId,
            subjectLabel = subjectLabel,
            predicateId = predicateId,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            objectClasses = objectClasses,
            objectId = objectId,
            objectLabel = objectLabel
        )

    override fun create(subject: ThingId, predicate: ThingId, `object`: ThingId): StatementId =
        create(ContributorId.UNKNOWN, subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
        subject: ThingId,
        predicate: ThingId,
        `object`: ThingId,
        modifiable: Boolean
    ): StatementId {
        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { StatementSubjectNotFound(subject) }

        if (foundSubject is Resource) {
            if (Classes.rosettaStoneStatement in foundSubject.classes) {
                throw InvalidStatement.includesRosettaStoneStatementResource()
            } else if (predicate == Predicates.hasListElement && Classes.list in foundSubject.classes) {
                throw InvalidStatement.isListElementStatement()
            }
        }

        val foundPredicate = predicateService.findById(predicate)
            .orElseThrow { StatementPredicateNotFound(predicate) }

        val foundObject = thingRepository.findByThingId(`object`)
            .orElseThrow { StatementObjectNotFound(`object`) }

        val statement = statementRepository.findAll(
            subjectId = subject,
            predicateId = predicate,
            objectId = `object`,
            pageable = PageRequests.SINGLE
        )
        if (!statement.isEmpty) {
            return statement.single().id
        }
        val id = statementRepository.nextIdentity()
        val newStatement = GeneralStatement(
            id = id,
            subject = foundSubject,
            predicate = foundPredicate,
            `object` = foundObject,
            createdBy = userId,
            createdAt = OffsetDateTime.now(clock),
            modifiable = modifiable
        )
        statementRepository.save(newStatement)
        return id
    }

    override fun add(
        userId: ContributorId,
        subject: ThingId,
        predicate: ThingId,
        `object`: ThingId,
        modifiable: Boolean
    ) {
        // This method mostly exists for performance reasons. We just create the statement but do not return anything.
        // That saves the extra calls to the database to retrieve the statement again, even if it may not be needed.

        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { StatementSubjectNotFound(subject) }

        if (foundSubject is Resource) {
            if (Classes.rosettaStoneStatement in foundSubject.classes) {
                throw InvalidStatement.includesRosettaStoneStatementResource()
            } else if (predicate == Predicates.hasListElement && Classes.list in foundSubject.classes) {
                throw InvalidStatement.isListElementStatement()
            }
        }

        val foundPredicate = predicateService.findById(predicate)
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
            createdAt = OffsetDateTime.now(clock),
            modifiable = modifiable
        )
        statementRepository.save(statement)
    }

    override fun totalNumberOfStatements(): Long = statementRepository.count()

    /**
     * Deletes a statement.
     *
     * @param statementId the ID of the statement to delete.
     */
    override fun delete(statementId: StatementId) {
        statementRepository.findByStatementId(statementId).ifPresent {
            if (!it.modifiable) throw StatementNotModifiable(it.id)
            if (it.predicate.id == Predicates.hasListElement && it.subject is Resource && Classes.list in (it.subject as Resource).classes) {
                throw ForbiddenStatementDeletion.usedInList()
            }
            statementRepository.deleteByStatementId(statementId)
        }
    }

    /**
     * Deletes a set of statements.
     *
     * @param statementIds the set of IDs of the statements to delete.
     */
    override fun delete(statementIds: Set<StatementId>) {
        statementRepository.findAllByStatementIdIn(statementIds, PageRequests.ALL).forEach {
            if (!it.modifiable) throw StatementNotModifiable(it.id)
            if (it.predicate.id == Predicates.hasListElement && it.subject is Resource && Classes.list in (it.subject as Resource).classes) {
                throw ForbiddenStatementDeletion.usedInList()
            }
        }
        statementRepository.deleteByStatementIds(statementIds)
    }

    override fun update(command: UpdateStatementUseCase.UpdateCommand) {
        var found = statementRepository.findByStatementId(command.statementId)
            .orElseThrow { StatementNotFound(command.statementId.value) }

        if (!found.modifiable) {
            throw StatementNotModifiable(found.id)
        }

        if (found.predicate.id == Predicates.hasListElement && found.subject is Resource && Classes.list in (found.subject as Resource).classes) {
            throw InvalidStatement.isListElementStatement()
        }

        val literal: Thing? = found.`object`.takeIf { it is Literal }

        command.subjectId?.let {
            //  This method needs a re-write.
            val foundSubject = thingRepository.findByThingId(it)
                .orElseThrow { StatementSubjectNotFound(it) }

            if (foundSubject is Resource) {
                if (Classes.rosettaStoneStatement in foundSubject.classes) {
                    throw InvalidStatement.includesRosettaStoneStatementResource()
                } else if ((found.predicate.id == Predicates.hasListElement || command.predicateId == Predicates.hasListElement) && Classes.list in foundSubject.classes) {
                    throw InvalidStatement.isListElementStatement()
                }
            }
            // found = found.copy(subject = foundSubject) // TODO: does this make sense?
        }

        command.predicateId?.let {
            val foundPredicate = predicateService.findById(it)
                .orElseThrow { StatementPredicateNotFound(it) }
            found = found.copy(predicate = foundPredicate)
        }

        command.objectId?.let {
            val foundObject = thingRepository.findByThingId(it)
                .orElseThrow { StatementObjectNotFound(it) }
            found = found.copy(`object` = foundObject)
        }

        statementRepository.deleteByStatementId(command.statementId)
        // Restore literal that may have automatically been deleted by statement deletion
        if (literal != null && command.objectId != null) {
            literalRepository.save(literal as Literal)
        }
        statementRepository.save(found)
    }

    override fun countStatements(paperId: ThingId): Long = statementRepository.countByIdRecursive(paperId)

    override fun fetchAsBundle(
        thingId: ThingId,
        configuration: BundleConfiguration,
        includeFirst: Boolean,
        sort: Sort
    ): Bundle {
        if (thingRepository.findByThingId(thingId).isEmpty) {
            throw ThingNotFound(thingId)
        }
        return when (includeFirst) {
            true -> createBundleFirstIncluded(thingId, configuration, sort)
            false -> createBundle(thingId, configuration, sort)
        }
    }

    override fun countPredicateUsage(pageable: Pageable) =
        statementRepository.countPredicateUsage(pageable)

    override fun countIncomingStatements(id: ThingId) =
        statementRepository.countIncomingStatements(id)

    override fun countIncomingStatements(ids: Set<ThingId>): Map<ThingId, Long> =
        statementRepository.countIncomingStatements(ids)

    override fun findAllDescriptions(ids: Set<ThingId>): Map<ThingId, String> =
        statementRepository.findAllDescriptions(ids)

    /**
     * Create a bundle where the first level is not included in the statements.
     *
     * @param thingId the root thing to start from.
     * @param configuration the bundle configuration passed down.
     *
     * @return returns a Bundle object
     */
    private fun createBundle(
        thingId: ThingId,
        configuration: BundleConfiguration,
        sort: Sort
    ): Bundle = Bundle(
        thingId, statementRepository.fetchAsBundle(thingId, configuration, sort).toMutableList()
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
        thingId: ThingId,
        configuration: BundleConfiguration,
        sort: Sort
    ): Bundle =
        createBundle(thingId, configuration, sort).merge(
            Bundle(
                thingId,
                statementRepository.fetchAsBundle(thingId, BundleConfiguration.firstLevelConf(), sort).toMutableList()
            ), sort
        )

    override fun removeAll() = statementRepository.deleteAll()
}
