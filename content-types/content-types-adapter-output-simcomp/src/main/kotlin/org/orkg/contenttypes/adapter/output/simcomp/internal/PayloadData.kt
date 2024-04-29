package org.orkg.contenttypes.adapter.output.simcomp.internal

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement

data class PayloadData(
    val rootResource: ThingId,
    val statements: List<GeneralStatement>
)
