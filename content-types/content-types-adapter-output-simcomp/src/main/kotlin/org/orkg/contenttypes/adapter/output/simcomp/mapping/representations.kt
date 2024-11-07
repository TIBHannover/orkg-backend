package org.orkg.contenttypes.adapter.output.simcomp.mapping

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.StatementRepresentation

data class PublishedContentTypeRepresentation(
    @field:JsonProperty("rootResource")
    val rootId: ThingId,
    @field:JsonProperty("statements")
    val subgraph: List<StatementRepresentation>
)
