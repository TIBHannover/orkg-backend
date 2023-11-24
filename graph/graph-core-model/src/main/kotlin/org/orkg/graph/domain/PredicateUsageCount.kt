package org.orkg.graph.domain

import org.orkg.common.ThingId

data class PredicateUsageCount(
    val id: ThingId,
    val count: Long
)
