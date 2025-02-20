package org.orkg.contenttypes.domain

import org.orkg.graph.domain.Resource

data class ResearchFieldWithChildCount(
    val resource: Resource,
    val childCount: Long,
)
