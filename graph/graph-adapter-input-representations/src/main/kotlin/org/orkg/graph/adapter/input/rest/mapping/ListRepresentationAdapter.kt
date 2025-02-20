package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.adapter.input.rest.ListRepresentation
import org.orkg.graph.domain.List
import org.springframework.data.domain.Page
import java.util.Optional

interface ListRepresentationAdapter {
    fun Optional<List>.mapToListRepresentation(): Optional<ListRepresentation> =
        map { it.toListRepresentation() }

    fun Page<List>.mapToListRepresentation(): Page<ListRepresentation> =
        map { it.toListRepresentation() }

    fun List.toListRepresentation(): ListRepresentation =
        ListRepresentation(id, label, elements, createdAt, createdBy, modifiable)
}
