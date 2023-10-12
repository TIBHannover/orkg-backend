package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.ListRepresentation
import eu.tib.orkg.prototype.statements.domain.model.List
import java.util.*
import org.springframework.data.domain.Page

interface ListRepresentationAdapter {

    fun Optional<List>.mapToListRepresentation(): Optional<ListRepresentation> =
        map { it.toListRepresentation() }

    fun Page<List>.mapToListRepresentation(): Page<ListRepresentation> =
        map { it.toListRepresentation() }

    fun List.toListRepresentation(): ListRepresentation =
        ListRepresentation(id, label, elements, createdAt, createdBy)
}
