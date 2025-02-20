package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ListUseCases :
    CreateListUseCase,
    RetrieveListUseCase,
    UpdateListUseCase,
    DeleteListUseCase

interface RetrieveListUseCase {
    fun findById(id: ThingId): Optional<org.orkg.graph.domain.List>

    fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing>

    fun existsById(id: ThingId): Boolean
}

interface CreateListUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        val elements: List<ThingId>,
        val id: ThingId? = null,
        val modifiable: Boolean = true,
    )
}

interface UpdateListUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val label: String? = null,
        val elements: List<ThingId>? = null,
    )
}

interface DeleteListUseCase {
    fun deleteById(id: ThingId)
}
