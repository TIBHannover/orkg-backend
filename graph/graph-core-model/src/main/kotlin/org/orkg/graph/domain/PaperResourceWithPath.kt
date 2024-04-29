package org.orkg.graph.domain

typealias Path = kotlin.collections.List<kotlin.collections.List<Thing>>

data class PaperResourceWithPath(
    val paper: Resource,
    var path: Path,
)
