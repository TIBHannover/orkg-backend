package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateStatementUseCase {
    // legacy methods:
    fun create(subject: ThingId, predicate: ThingId, `object`: ThingId): StatementRepresentation
    fun create(userId: ContributorId, subject: ThingId, predicate: ThingId, `object`: ThingId): StatementRepresentation
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
    fun remove(statementId: StatementId)
    fun removeAll()
}
