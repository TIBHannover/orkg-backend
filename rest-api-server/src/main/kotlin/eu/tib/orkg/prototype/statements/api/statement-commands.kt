package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.StatementEditRequest
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId

interface CreateStatementUseCase {
    fun create(subject: String, predicate: PredicateId, `object`: String): GeneralStatement
    fun create(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String): GeneralStatement
    fun add(userId: ContributorId, subject: String, predicate: PredicateId, `object`: String)
}

interface UpdateStatementUseCase {
    fun update(statementEditRequest: StatementEditRequest): GeneralStatement
}

interface DeleteStatementUseCase {
    fun remove(statementId: StatementId)
    fun removeAll()
}
