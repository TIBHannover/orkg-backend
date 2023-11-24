package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreateListUseCase {
    fun create(command: CreateListUseCase.CreateCommand): ThingId

    data class CreateCommand(
        val label: String,
        val elements: List<ThingId>,
        val id: ThingId? = null,
        val contributorId: ContributorId? = null,
    )
}

interface UpdateListUseCase {
    fun update(id: ThingId, command: UpdateListUseCase.UpdateCommand)

    data class UpdateCommand(
        val label: String? = null,
        val elements: List<ThingId>? = null,
        val contributorId: ContributorId? = null
    )
}

interface DeleteListUseCase {
    fun delete(id: ThingId)
}
