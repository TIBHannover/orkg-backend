package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.adapter.input.rest.RdfFileRepresentation
import org.orkg.dataimport.domain.rdf.RdfFile
import org.springframework.data.domain.Page
import java.util.Optional

interface RdfFileRepresentationAdapter {
    fun Optional<RdfFile>.mapToRdfFileRepresentation(): Optional<RdfFileRepresentation> =
        map { it.toRdfFileRepresentation() }

    fun Page<RdfFile>.mapToRdfFileRepresentation(): Page<RdfFileRepresentation> =
        map { it.toRdfFileRepresentation() }

    fun RdfFile.toRdfFileRepresentation(): RdfFileRepresentation =
        RdfFileRepresentation(id, name, baseUri, format, state, createdBy, createdAt)
}
