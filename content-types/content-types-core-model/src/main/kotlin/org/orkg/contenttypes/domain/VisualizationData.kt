package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import tools.jackson.databind.JsonNode

data class VisualizationData(
    val id: ThingId,
    val data: JsonNode,
) {
    companion object {
        fun from(node: JsonNode): VisualizationData =
            VisualizationData(ThingId(node["orkgOrigin"].stringValue(null)), node)
    }
}
