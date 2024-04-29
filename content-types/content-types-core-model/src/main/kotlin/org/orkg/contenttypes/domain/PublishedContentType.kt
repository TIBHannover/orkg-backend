package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement

data class PublishedContentType(
    val rootId: ThingId,
    val subgraph: List<GeneralStatement>
)
