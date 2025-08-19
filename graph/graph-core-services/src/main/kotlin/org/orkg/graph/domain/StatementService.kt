package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class StatementService(
    private val thingRepository: ThingRepository,
    private val predicateService: PredicateRepository,
    private val statementRepository: StatementRepository,
    private val literalRepository: LiteralRepository,
    private val clock: Clock,
) : StatementUseCases {
    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        statementRepository.findByStatementId(statementId)

    @TransactionalOnNeo4j(readOnly = true)
    override fun existsById(id: StatementId): Boolean = statementRepository.existsById(id)

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
        objectLabel: String?,
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

    override fun create(command: CreateStatementUseCase.CreateCommand): StatementId {
        val subject = thingRepository.findById(command.subjectId)
            .orElseThrow { StatementSubjectNotFound(command.subjectId) }
        validateSubject(subject, command.predicateId)
        val predicate = predicateService.findById(command.predicateId)
            .orElseThrow { StatementPredicateNotFound(command.predicateId) }
        val `object` = thingRepository.findById(command.objectId)
            .orElseThrow { StatementObjectNotFound(command.objectId) }
        val existing = statementRepository.findAll(
            subjectId = command.subjectId,
            predicateId = command.predicateId,
            objectId = command.objectId,
            pageable = PageRequests.SINGLE
        ).singleOrNull()
        if (existing != null) {
            if (command.id != null && command.id != existing.id) {
                throw StatementAlreadyExists(existing.id)
            }
            return existing.id
        }
        val id = command.id
            ?.also { id -> statementRepository.findByStatementId(id).ifPresent { throw StatementAlreadyExists(id) } }
            ?: statementRepository.nextIdentity()
        val statement = GeneralStatement(
            id = id,
            subject = subject,
            predicate = predicate,
            `object` = `object`,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
            modifiable = command.modifiable
        )
        statementRepository.save(statement)
        return id
    }

    /**
     * Deletes a statement.
     *
     * @param statementId the ID of the statement to delete.
     */
    override fun deleteById(statementId: StatementId) {
        statementRepository.findByStatementId(statementId).ifPresent {
            if (!it.modifiable) throw StatementNotModifiable(it.id)
            if (it.predicate.id == Predicates.hasListElement && it.subject is Resource && Classes.list in (it.subject as Resource).classes) {
                throw StatementInUse.usedInList()
            }
            statementRepository.deleteByStatementId(statementId)
        }
    }

    /**
     * Deletes a set of statements.
     *
     * @param statementIds the set of IDs of the statements to delete.
     */
    override fun deleteAllById(statementIds: Set<StatementId>) {
        statementRepository.findAllByStatementIdIn(statementIds, PageRequests.ALL).forEach {
            if (!it.modifiable) throw StatementNotModifiable(it.id)
            if (it.predicate.id == Predicates.hasListElement && it.subject is Resource && Classes.list in (it.subject as Resource).classes) {
                throw StatementInUse.usedInList()
            }
        }
        statementRepository.deleteByStatementIds(statementIds)
    }

    override fun update(command: UpdateStatementUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val statement = statementRepository.findByStatementId(command.statementId)
            .orElseThrow { StatementNotFound(command.statementId.value) }
        if (!statement.modifiable) {
            throw StatementNotModifiable(statement.id)
        }
        if (statement.predicate.id == Predicates.hasListElement && statement.subject is Resource && Classes.list in (statement.subject as Resource).classes) {
            throw InvalidStatement.isListElementStatement()
        }
        val updated = statement.apply(command, thingRepository, predicateService, { subject ->
            if (subject is Resource) {
                if (Classes.rosettaStoneStatement in subject.classes) {
                    throw InvalidStatement.includesRosettaStoneStatementResource()
                } else if ((statement.predicate.id == Predicates.hasListElement || command.predicateId == Predicates.hasListElement) && Classes.list in subject.classes) {
                    throw InvalidStatement.isListElementStatement()
                }
            }
        })
        if (updated != statement) {
            statementRepository.deleteByStatementId(command.statementId)
            // restore literal that may have automatically been deleted by statement deletion
            if (statement.`object` is Literal && updated.`object` == statement.`object`) {
                literalRepository.save(statement.`object` as Literal)
            }
            statementRepository.save(updated)
        }
    }

    override fun countStatementsInPaperSubgraph(paperId: ThingId): Long =
        statementRepository.countStatementsInPaperSubgraph(paperId)

    override fun fetchAsBundle(
        thingId: ThingId,
        configuration: BundleConfiguration,
        includeFirst: Boolean,
        sort: Sort,
    ): Bundle {
        if (thingRepository.findById(thingId).isEmpty) {
            throw ThingNotFound(thingId)
        }
        return when (includeFirst) {
            true -> createBundleFirstIncluded(thingId, configuration, sort)
            false -> createBundle(thingId, configuration, sort)
        }
    }

    override fun countIncomingStatementsById(id: ThingId) =
        statementRepository.countIncomingStatementsById(id)

    override fun countAllIncomingStatementsById(ids: Set<ThingId>): Map<ThingId, Long> =
        statementRepository.countAllIncomingStatementsById(ids)

    override fun findAllDescriptionsById(ids: Set<ThingId>): Map<ThingId, String> =
        statementRepository.findAllDescriptionsById(ids)

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
        sort: Sort,
    ): Bundle = Bundle(
        thingId,
        statementRepository.fetchAsBundle(thingId, configuration, sort).toMutableList()
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
        sort: Sort,
    ): Bundle =
        createBundle(thingId, configuration, sort).merge(
            Bundle(
                thingId,
                statementRepository.fetchAsBundle(thingId, BundleConfiguration.firstLevelConf(), sort).toMutableList()
            ),
            sort
        )

    override fun deleteAll() = statementRepository.deleteAll()

    private fun validateSubject(subject: Thing, predicateId: ThingId) {
        if (subject is Resource) {
            if (Classes.rosettaStoneStatement in subject.classes) {
                throw InvalidStatement.includesRosettaStoneStatementResource()
            } else if (predicateId == Predicates.hasListElement && Classes.list in subject.classes) {
                throw InvalidStatement.isListElementStatement()
            }
        } else if (subject is Literal) {
            throw InvalidStatement.subjectMustNotBeLiteral()
        }
    }
}
