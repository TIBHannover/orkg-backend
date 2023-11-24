package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.List
import org.orkg.graph.input.ListRepresentation
import org.springframework.data.domain.Page

interface ListRepresentationAdapter {

    fun Optional<List>.mapToListRepresentation(): Optional<ListRepresentation> =
        map { it.toListRepresentation() }

    fun Page<List>.mapToListRepresentation(): Page<ListRepresentation> =
        map { it.toListRepresentation() }

    fun List.toListRepresentation(): ListRepresentation =
        ListRepresentation(id, label, elements, createdAt, createdBy)
}
