package org.orkg.graph.input

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreateClassUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val label: String,
        val id: ThingId? = null,
        val contributorId: ContributorId? = null,
        val uri: URI? = null,
        val modifiable: Boolean = true
    )
}

interface UpdateClassUseCase {
    fun update(command: UpdateCommand)
    fun replace(command: ReplaceCommand)

    data class UpdateCommand(
        val id: ThingId,
        val label: String? = null,
        val uri: URI? = null
    )

    data class ReplaceCommand(
        val id: ThingId,
        val label: String,
        val uri: URI?,
    )
}

interface DeleteClassUseCase {
    // legacy methods:
    fun removeAll()
}
