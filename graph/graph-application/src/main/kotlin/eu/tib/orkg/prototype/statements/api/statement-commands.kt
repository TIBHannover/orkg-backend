package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateStatementUseCase {
    // legacy methods:
    fun create(subject: ThingId, predicate: ThingId, `object`: ThingId): StatementId
    fun create(userId: ContributorId, subject: ThingId, predicate: ThingId, `object`: ThingId): StatementId
    fun add(userId: ContributorId, subject: ThingId, predicate: ThingId, `object`: ThingId)
}

interface UpdateStatementUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val statementId: StatementId,
        val subjectId: ThingId? = null,
        val predicateId: ThingId? = null,
        val objectId: ThingId? = null,
    )
}

interface DeleteStatementUseCase {
    fun delete(statementId: StatementId)
    fun delete(statementIds: Set<StatementId>)

    // For tests only!
    fun removeAll()
}
