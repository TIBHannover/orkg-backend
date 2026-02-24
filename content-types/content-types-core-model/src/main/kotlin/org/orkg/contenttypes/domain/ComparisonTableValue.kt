package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

data class ComparisonTableValue(
    val value: Thing,
    val children: Map<ThingId, List<ComparisonTableValue>>,
)
