package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals

interface CreateLiteralUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val id: ThingId? = null,
        val contributorId: ContributorId,
        val label: String,
        val datatype: String = Literals.XSD.STRING.prefixedUri,
        val modifiable: Boolean = true
    )
}

interface UpdateLiteralUseCase {
    // legacy methods:
    fun update(literal: Literal)
}

interface DeleteLiteralUseCase {
    // legacy methods:
    fun removeAll()
}
