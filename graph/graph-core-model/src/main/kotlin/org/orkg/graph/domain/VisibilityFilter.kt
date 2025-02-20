package org.orkg.graph.domain

enum class VisibilityFilter(
    val targets: Set<Visibility>,
) {
    ALL_LISTED(setOf(Visibility.DEFAULT, Visibility.FEATURED)),
    UNLISTED(setOf(Visibility.UNLISTED)),
    FEATURED(setOf(Visibility.FEATURED)),
    NON_FEATURED(setOf(Visibility.DEFAULT)),
    DELETED(setOf(Visibility.DELETED)),
    ;

    companion object {
        fun fromFlags(
            featured: Boolean?,
            unlisted: Boolean?,
        ): VisibilityFilter =
            when (unlisted == true) {
                true -> UNLISTED
                false ->
                    when (featured) {
                        null -> ALL_LISTED
                        true -> FEATURED
                        false -> NON_FEATURED
                    }
            }
    }
}
