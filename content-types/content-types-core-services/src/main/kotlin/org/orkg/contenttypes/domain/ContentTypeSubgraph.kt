package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement

internal data class ContentTypeSubgraph(
    val root: ThingId,
    val statements: Map<ThingId, List<GeneralStatement>>
)
