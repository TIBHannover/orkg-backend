package org.orkg.graph.input

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreateClassUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val label: String,
        val id: ThingId? = null,
        val contributorId: ContributorId? = null,
        val uri: ParsedIRI? = null,
        val modifiable: Boolean = true
    )
}

interface UpdateClassUseCase {
    fun update(command: UpdateCommand)
    fun replace(command: ReplaceCommand)

    data class UpdateCommand(
        val id: ThingId,
        val label: String? = null,
        val uri: ParsedIRI? = null
    )

    data class ReplaceCommand(
        val id: ThingId,
        val label: String,
        val uri: ParsedIRI?,
    )
}

interface DeleteClassUseCase {
    // legacy methods:
    fun removeAll()
}
