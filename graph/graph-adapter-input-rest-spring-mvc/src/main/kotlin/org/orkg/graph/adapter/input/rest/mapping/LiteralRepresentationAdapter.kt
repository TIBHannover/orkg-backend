package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.Literal
import org.orkg.graph.input.LiteralRepresentation
import org.springframework.data.domain.Page

interface LiteralRepresentationAdapter {

    fun Optional<Literal>.mapToLiteralRepresentation(): Optional<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Page<Literal>.mapToLiteralRepresentation(): Page<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Literal.toLiteralRepresentation(): LiteralRepresentation =
        LiteralRepresentation(id, label, datatype, createdAt, createdBy, modifiable)
}
