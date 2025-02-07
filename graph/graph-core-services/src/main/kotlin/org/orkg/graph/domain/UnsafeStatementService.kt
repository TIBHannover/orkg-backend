package org.orkg.graph.domain

import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UnsafeStatementService(
    private val thingRepository: ThingRepository,
    private val predicateRepository: PredicateRepository,
    private val statementRepository: StatementRepository,
    private val literalRepository: LiteralRepository,
    private val clock: Clock,
) : UnsafeStatementUseCases {

    override fun create(command: CreateStatementUseCase.CreateCommand): StatementId {
        val subject = thingRepository.findByThingId(command.subjectId)
            .orElseThrow { StatementSubjectNotFound(command.subjectId) }
        val predicate = predicateRepository.findById(command.predicateId)
            .orElseThrow { StatementPredicateNotFound(command.predicateId) }
        val `object` = thingRepository.findByThingId(command.objectId)
            .orElseThrow { StatementObjectNotFound(command.objectId) }
        val statement = GeneralStatement(
            id = command.id ?: statementRepository.nextIdentity(),
            subject = subject,
            predicate = predicate,
            `object` = `object`,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
            modifiable = command.modifiable
        )
        statementRepository.save(statement)
        return statement.id
    }

    override fun update(command: UpdateStatementUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val statement = statementRepository.findByStatementId(command.statementId)
            .orElseThrow { StatementNotFound(command.statementId.value) }
        val updated = statement.apply(command, thingRepository, predicateRepository)
        if (updated != statement) {
            statementRepository.deleteByStatementId(command.statementId)
            // restore literal that may have automatically been deleted by statement deletion
            if (statement.`object` is Literal && updated.`object` == statement.`object`) {
                literalRepository.save(statement.`object` as Literal)
            }
            statementRepository.save(updated)
        }
    }

    override fun delete(statementId: StatementId) {
        statementRepository.deleteByStatementId(statementId)
    }

    override fun delete(statementIds: Set<StatementId>) {
        statementRepository.deleteByStatementIds(statementIds)
    }

    override fun removeAll() = statementRepository.deleteAll()
}
