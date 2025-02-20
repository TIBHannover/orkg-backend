package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.adapter.input.rest.LiteralRepresentation
import org.orkg.graph.domain.Literal
import org.springframework.data.domain.Page
import java.util.Optional

interface LiteralRepresentationAdapter {
    fun Optional<Literal>.mapToLiteralRepresentation(): Optional<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Page<Literal>.mapToLiteralRepresentation(): Page<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Literal.toLiteralRepresentation(): LiteralRepresentation =
        LiteralRepresentation(id, label, datatype, createdAt, createdBy, modifiable)
}
