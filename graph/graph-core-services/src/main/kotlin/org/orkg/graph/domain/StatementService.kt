package org.orkg.graph.domain

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
) : StatementUseCases {

    override fun findAll(pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAll(pagination)

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)

    override fun findAllBySubject(subjectId: ThingId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllBySubject(subjectId, pagination)

    override fun findAllByPredicate(predicateId: ThingId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByPredicateId(predicateId, pagination)

    override fun findAllByObject(objectId: ThingId, pagination: Pageable): Page<GeneralStatement> =
        statementRepository.findAllByObject(objectId, pagination)

    override fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<GeneralStatement> = statementRepository.findAllBySubjectAndPredicate(subjectId, predicateId, pagination)

    override fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pagination: Pageable
    ): Page<GeneralStatement> = statementRepository.findAllByObjectAndPredicate(objectId, predicateId, pagination)

    @Transactional(readOnly = true)
    override fun exists(id: StatementId): Boolean = statementRepository.exists(id)

    override fun create(subject: ThingId, predicate: ThingId, `object`: ThingId): StatementId =
        create(ContributorId.createUnknownContributor(), subject, predicate, `object`)

    override fun create(
        userId: ContributorId,
        subject: ThingId,
        predicate: ThingId,
        `object`: ThingId
    ): StatementId {
        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { StatementSubjectNotFound(subject) }

        if (predicate == Predicates.hasListElement && foundSubject is Resource && Classes.list in foundSubject.classes) {
            throw ForbiddenStatementSubject.isList()
        }

        val foundPredicate = predicateService.findById(predicate)
            .orElseThrow { StatementPredicateNotFound(predicate) }

        val foundObject = thingRepository.findByThingId(`object`)
            .orElseThrow { StatementObjectNotFound(`object`) }

        val statement = statementRepository.findBySubjectIdAndPredicateIdAndObjectId(subject, predicate, `object`)
        if (statement.isPresent) {
            return statement.get().id!!
        }
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
        return id
    }

    override fun add(userId: ContributorId, subject: ThingId, predicate: ThingId, `object`: ThingId) {
        // This method mostly exists for performance reasons. We just create the statement but do not return anything.
        // That saves the extra calls to the database to retrieve the statement again, even if it may not be needed.

        val foundSubject = thingRepository.findByThingId(subject)
            .orElseThrow { StatementSubjectNotFound(subject) }

        if (predicate == Predicates.hasListElement && foundSubject is Resource && Classes.list in foundSubject.classes) {
            throw ForbiddenStatementSubject.isList()
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
            createdAt = OffsetDateTime.now(),
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
            if (it.predicate.id == Predicates.hasListElement && it.subject is Resource && Classes.list in (it.subject as Resource).classes) {
                throw ForbiddenStatementDeletion.usedInList()
            }
        }
        statementRepository.deleteByStatementIds(statementIds)
    }

    override fun update(command: UpdateStatementUseCase.UpdateCommand) {
        var found = statementRepository.findByStatementId(command.statementId)
            .orElseThrow { StatementNotFound(command.statementId.value) }

        if (found.predicate.id == Predicates.hasListElement && found.subject is Resource && Classes.list in (found.subject as Resource).classes) {
            throw UnmodifiableStatement.subjectIsList()
        }

        val literal: Thing? = found.`object`.takeIf { it is Literal }

        command.subjectId?.let {
            //  This method needs a re-write.
            val foundSubject = thingRepository.findByThingId(it)
                .orElseThrow { StatementSubjectNotFound(it) }

            if ((found.predicate.id == Predicates.hasListElement || command.predicateId == Predicates.hasListElement)
                && foundSubject is Resource && Classes.list in foundSubject.classes
            ) {
                throw ForbiddenStatementSubject.isList()
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

    override fun findAllByPredicateAndLabel(
        predicateId: ThingId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> = statementRepository.findAllByPredicateIdAndLabel(predicateId, literal, pagination)

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pagination: Pageable
    ): Page<GeneralStatement> =
        statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
            predicateId,
            literal,
            subjectClass,
            pagination
        )

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

    override fun countStatementsAboutResource(id: ThingId) =
        statementRepository.countStatementsAboutResource(id)

    override fun countStatementsAboutResources(ids: Set<ThingId>): Map<ThingId, Long> =
        statementRepository.countStatementsAboutResources(ids)

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
