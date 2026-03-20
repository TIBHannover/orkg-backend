package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.input.rest.PublishedPaperContentsRepresentation
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import java.util.Optional

interface PublishedPaperContentsRepresentationAdapter : StatementRepresentationAdapter {
    fun Optional<PublishedContentType>.mapToPublishedPaperContentsRepresentation(mediaTypeCapabilities: MediaTypeCapabilities) =
        map { it.toPublishedPaperContentsRepresentation(mediaTypeCapabilities) }

    fun PublishedContentType.toPublishedPaperContentsRepresentation(
        mediaTypeCapabilities: MediaTypeCapabilities,
    ): PublishedPaperContentsRepresentation =
        PublishedPaperContentsRepresentation(
            rootId,
            subgraph.mapToStatementRepresentation(mediaTypeCapabilities).toList(),
        )
}
