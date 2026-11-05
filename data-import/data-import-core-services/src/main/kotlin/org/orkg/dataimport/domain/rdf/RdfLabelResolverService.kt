package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Resource
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RdfLabelResolverService(
    @param:Qualifier("rdfLabelResolvers")
    private val rdfLabelResolvers: List<RdfLabelResolver>,
) {
    fun resolveLabel(rdfFileId: UUID, input: Resource): String {
        if (input.isBNode) {
            return ""
        }
        rdfLabelResolvers.forEach { provider ->
            if (provider.canResolve(rdfFileId, input)) {
                val label = provider.resolve(rdfFileId, input)
                if (label != null) {
                    return sanitize(label)
                }
            }
        }
        return sanitize(input.stringValue().substringAfterLast("#"))
    }

    internal fun sanitize(label: String): String =
        label.replace("\n", " ").take(MAX_LABEL_LENGTH)
}
