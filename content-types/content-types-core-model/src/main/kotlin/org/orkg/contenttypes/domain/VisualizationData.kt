package org.orkg.contenttypes.domain

import com.fasterxml.jackson.databind.JsonNode
import org.orkg.common.ThingId

data class VisualizationData(
    val id: ThingId,
    val data: JsonNode,
) {
    companion object {
        fun from(node: JsonNode): VisualizationData =
            VisualizationData(ThingId(node["orkgOrigin"].textValue()), node)
    }
}
