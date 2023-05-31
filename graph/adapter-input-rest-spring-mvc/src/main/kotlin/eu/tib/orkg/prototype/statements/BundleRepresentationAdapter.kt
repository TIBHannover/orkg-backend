package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.BundleRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Bundle

interface BundleRepresentationAdapter : StatementRepresentationAdapter {
    fun Bundle.toBundleRepresentation(): BundleRepresentation =
        BundleRepresentation(
            rootId = this@toBundleRepresentation.rootId,
            bundle = this@toBundleRepresentation.bundle.mapToStatementRepresentation().toList()
        )
}
