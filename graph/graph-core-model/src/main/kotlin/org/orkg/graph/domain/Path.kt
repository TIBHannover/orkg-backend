package org.orkg.graph.domain

import kotlin.collections.List

typealias Path = List<Thing>

enum class PathDirection {
    INCOMING,
    OUTGOING,
    UNDIRECTED,
}
