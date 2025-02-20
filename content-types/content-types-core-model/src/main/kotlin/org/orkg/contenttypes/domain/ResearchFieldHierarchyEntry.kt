package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource

data class ResearchFieldHierarchyEntry(
    val resource: Resource,
    val parentIds: Set<ThingId>,
)
