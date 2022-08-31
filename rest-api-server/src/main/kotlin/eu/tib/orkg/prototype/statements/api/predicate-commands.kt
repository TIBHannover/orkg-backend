package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.PredicateId

interface CreatePredicateUseCase {
    // legacy methods:
    fun create(label: String): PredicateRepresentation
    fun create(userId: ContributorId, label: String): PredicateRepresentation
    fun create(request: CreatePredicateRequest): PredicateRepresentation
    fun create(userId: ContributorId, request: CreatePredicateRequest): PredicateRepresentation
    fun createIfNotExists(id: PredicateId, label: String)
}

interface UpdatePredicateUseCase {
    fun update(id: PredicateId, command: ReplaceCommand)

    data class ReplaceCommand(
        val label: String,
        val description: String? = null,
    )
}

interface DeletePredicateUseCase {
    // legacy methods:
    fun removeAll()
}
