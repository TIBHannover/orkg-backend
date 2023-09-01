package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateListUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val label: String,
        val elements: List<ThingId>,
        val id: ThingId? = null,
        val contributorId: ContributorId? = null,
    )
}

interface UpdateListUseCase {
    fun update(id: ThingId, command: UpdateCommand)

    data class UpdateCommand(
        val label: String?,
        val elements: List<ThingId>?,
        val contributorId: ContributorId? = null
    )
}

interface DeleteListUseCase {
    fun delete(id: ThingId)
}
