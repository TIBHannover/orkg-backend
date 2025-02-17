package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreateListUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        val elements: List<ThingId>,
        val id: ThingId? = null,
        val modifiable: Boolean = true
    )
}

interface UpdateListUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val elements: List<ThingId>? = null
    )
}

interface DeleteListUseCase {
    fun deleteById(id: ThingId)
}
