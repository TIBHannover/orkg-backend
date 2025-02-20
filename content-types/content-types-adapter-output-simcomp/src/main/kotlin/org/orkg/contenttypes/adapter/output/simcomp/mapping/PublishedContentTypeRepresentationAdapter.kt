package org.orkg.contenttypes.adapter.output.simcomp.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.output.simcomp.representations.PublishedContentTypeRepresentation
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter

interface PublishedContentTypeRepresentationAdapter : StatementRepresentationAdapter {
    fun PublishedContentType.toPublishedContentTypeRepresentation(
        mediaTypeCapabilities: MediaTypeCapabilities,
    ): PublishedContentTypeRepresentation =
        PublishedContentTypeRepresentation(
            rootId,
            subgraph.mapToStatementRepresentation(mediaTypeCapabilities).toList()
        )
}
