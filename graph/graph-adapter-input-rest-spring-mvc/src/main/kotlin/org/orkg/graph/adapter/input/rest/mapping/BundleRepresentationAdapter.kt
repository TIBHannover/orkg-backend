package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.graph.adapter.input.rest.BundleRepresentation
import org.orkg.graph.domain.Bundle

interface BundleRepresentationAdapter : StatementRepresentationAdapter {
    fun Bundle.toBundleRepresentation(capabilities: MediaTypeCapabilities): BundleRepresentation =
        BundleRepresentation(rootId, bundle.mapToStatementRepresentation(capabilities).toList())
}
