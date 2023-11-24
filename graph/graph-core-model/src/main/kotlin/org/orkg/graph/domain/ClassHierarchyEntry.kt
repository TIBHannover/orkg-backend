package org.orkg.graph.domain

import org.orkg.common.ThingId

data class ClassHierarchyEntry(
    val `class`: Class,
    val parentId: ThingId?
)
