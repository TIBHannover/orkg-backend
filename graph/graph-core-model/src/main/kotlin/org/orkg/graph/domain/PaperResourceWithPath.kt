package org.orkg.graph.domain

import kotlin.collections.List

data class PaperResourceWithPath(
    val paper: Resource,
    var path: List<Path>,
)
