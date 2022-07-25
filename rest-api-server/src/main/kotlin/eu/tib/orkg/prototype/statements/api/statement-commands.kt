package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.StatementRepresentation

interface CreateStatementUseCase {
    fun create(subject: String, predicate: PredicateId, `object`: String): StatementRepresentation
    fun create(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String): StatementRepresentation
    fun add(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String)
}

interface UpdateStatementUseCase {
    fun update(statementEditRequest: StatementEditRequest): StatementRepresentation
}

interface DeleteStatementUseCase {
    fun remove(statementId: StatementId)
    fun removeAll()
}
