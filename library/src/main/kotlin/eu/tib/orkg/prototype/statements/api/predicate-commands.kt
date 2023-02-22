package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreatePredicateUseCase {
    fun create(command: CreateCommand): ThingId

    // legacy methods:
    fun create(label: String): PredicateRepresentation
    fun create(userId: ContributorId, label: String): PredicateRepresentation
    fun create(request: CreatePredicateRequest): PredicateRepresentation
    fun create(userId: ContributorId, request: CreatePredicateRequest): PredicateRepresentation
    fun createIfNotExists(id: ThingId, label: String)

    data class CreateCommand(
        val label: String,
        val id: String? = null,
        val contributorId: ContributorId? = null
    )
}

interface UpdatePredicateUseCase {
    fun update(id: ThingId, command: ReplaceCommand)

    data class ReplaceCommand(
        val label: String,
        val description: String? = null,
    )
}

interface DeletePredicateUseCase {
    // legacy methods:
    fun delete(predicateId: ThingId)
    fun removeAll()
}
