package org.orkg.community.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.graph.domain.PredicatePath

interface CreateObservatoryFilterUseCase {
    fun create(command: CreateCommand): ObservatoryFilterId

    data class CreateCommand(
        val id: ObservatoryFilterId? = null,
        val observatoryId: ObservatoryId,
        val label: String,
        val contributorId: ContributorId,
        val path: PredicatePath,
        val range: ThingId,
        val exact: Boolean,
        val featured: Boolean
    )
}

interface UpdateObservatoryFilterUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ObservatoryFilterId,
        val label: String?,
        val path: PredicatePath?,
        val range: ThingId?,
        val exact: Boolean?,
        val featured: Boolean?
    )
}

interface DeleteObservatoryFilterUseCase {
    fun deleteById(id: ObservatoryFilterId)
    fun deleteAll()
}
