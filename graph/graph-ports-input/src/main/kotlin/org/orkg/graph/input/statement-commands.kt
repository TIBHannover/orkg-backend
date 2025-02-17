package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.StatementId

interface CreateStatementUseCase {
    fun create(command: CreateCommand): StatementId

    data class CreateCommand(
        val id: StatementId? = null,
        val contributorId: ContributorId,
        val subjectId: ThingId,
        val predicateId: ThingId,
        val objectId: ThingId,
        val modifiable: Boolean = true
    )
}

interface UpdateStatementUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val statementId: StatementId,
        val contributorId: ContributorId,
        val subjectId: ThingId? = null,
        val predicateId: ThingId? = null,
        val objectId: ThingId? = null,
        val modifiable: Boolean? = null
    )
}

interface DeleteStatementUseCase {
    fun deleteById(statementId: StatementId)
    fun deleteAllById(statementIds: Set<StatementId>)

    // For tests only!
    fun deleteAll()
}
