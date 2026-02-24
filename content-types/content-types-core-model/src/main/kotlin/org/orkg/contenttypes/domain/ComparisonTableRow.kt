package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

data class ComparisonTableRow(
    val values: List<Thing?>,
    val children: Map<ThingId, List<ComparisonTableRow>>,
)
