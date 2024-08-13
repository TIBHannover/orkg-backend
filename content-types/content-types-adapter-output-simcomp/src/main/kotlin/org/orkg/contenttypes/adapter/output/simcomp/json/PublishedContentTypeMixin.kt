package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement

abstract class PublishedContentTypeMixin(
    @field:JsonProperty("rootResource")
    val rootId: ThingId,
    @field:JsonProperty("statements")
    val subgraph: List<GeneralStatement>
)
