package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateStatementUseCase {
    // legacy methods:
    fun create(subject: String, predicate: ThingId, `object`: String): StatementRepresentation
    fun create(userId: ContributorId, subject: String, predicate: ThingId, `object`: String): StatementRepresentation
    fun add(userId: ContributorId, subject: String, predicate: ThingId, `object`: String)
}

interface UpdateStatementUseCase {
    fun update(statementEditRequest: StatementEditRequest): StatementRepresentation
}

interface DeleteStatementUseCase {
    fun remove(statementId: StatementId)
    fun removeAll()
}
