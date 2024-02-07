package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreatePredicateUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val label: String,
        val contributorId: ContributorId? = null,
        val modifiable: Boolean = true
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
