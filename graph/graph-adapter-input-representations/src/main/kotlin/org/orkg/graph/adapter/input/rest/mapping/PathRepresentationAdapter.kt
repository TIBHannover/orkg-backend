package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.graph.adapter.input.rest.PathRepresentation
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Path
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page

interface PathRepresentationAdapter : ThingRepresentationAdapter {
    fun Page<Path>.mapToPathRepresentation(
        capabilities: MediaTypeCapabilities,
    ): Page<PathRepresentation> {
        val resources = content.flatten().filterIsInstance<Resource>()
        val statementCounts = countIncomingStatements(resources)
        val formattedLabelCount = formatLabelFor(resources, capabilities)
        val descriptions = findAllDescriptions(content.flatten().filter { it is Predicate || it is Class })
        return map { path ->
            path.map { thing ->
                thing.toThingRepresentation(statementCounts, formattedLabelCount, descriptions[thing.id])
            }
        }
    }
}
