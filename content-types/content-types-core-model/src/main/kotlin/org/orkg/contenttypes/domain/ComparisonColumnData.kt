package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

data class ComparisonColumnData(
    val title: Thing,
    val subtitle: Thing?,
    val values: Map<ThingId, List<ComparisonTableValue>>,
)
