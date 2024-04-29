package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.Bundle
import org.orkg.graph.adapter.input.rest.BundleRepresentation

interface BundleRepresentationAdapter : StatementRepresentationAdapter {
    fun Bundle.toBundleRepresentation(): BundleRepresentation =
        BundleRepresentation(rootId, bundle.mapToStatementRepresentation().toList())
}
