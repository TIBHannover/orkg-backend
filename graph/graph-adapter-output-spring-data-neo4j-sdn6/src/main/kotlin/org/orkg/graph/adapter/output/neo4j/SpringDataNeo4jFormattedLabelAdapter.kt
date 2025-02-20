package org.orkg.graph.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jFormattedLabelRepository
import org.orkg.graph.domain.TemplatedResource
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jFormattedLabelAdapter(
    private val neo4jRepository: Neo4jFormattedLabelRepository,
) : FormattedLabelRepository {
    override fun findTemplateSpecs(resourceIdToTemplateTargetClass: Map<ThingId, ThingId>): Map<ThingId, TemplatedResource> =
        neo4jRepository.findTemplateSpecs(resourceIdToTemplateTargetClass.entries.associate { it.key.value to it.value.value })
            .associateBy { it.id }
}
