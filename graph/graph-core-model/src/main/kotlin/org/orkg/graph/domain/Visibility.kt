package org.orkg.graph.domain

enum class Visibility {
    DEFAULT,
    UNLISTED,
    FEATURED,
    DELETED,
    ;

    fun toVisibilityFilter(): VisibilityFilter =
        when (this) {
            DEFAULT -> VisibilityFilter.NON_FEATURED
            UNLISTED -> VisibilityFilter.UNLISTED
            FEATURED -> VisibilityFilter.FEATURED
            DELETED -> VisibilityFilter.DELETED
        }
}
