package org.orkg.graph.adapter.output.neo4j

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jFormattedLabelRepository
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.TemplatedResource
import org.springframework.stereotype.Component
import java.util.*
import org.orkg.graph.output.FormattedLabelRepository

@Component
class SpringDataNeo4jFormattedLabelAdapter(
    private val neo4jRepository: Neo4jFormattedLabelRepository
) : FormattedLabelRepository {

    override fun findTemplateSpecs(id: ThingId): Optional<TemplatedResource> =
        neo4jRepository.findTemplateSpecs(id)

    override fun formattedLabelFor(id: ThingId, classes: Set<ThingId>): FormattedLabel? {
        if (classes.isEmpty()) return null
        val templatedResource = findTemplateSpecs(id)
        if (!templatedResource.isPresent) return null
        return FormattedLabel.ofOrNull(templatedResource.get().composeFormattedLabel())
    }
}
