package org.orkg.graph.adapter.input.rest

import org.orkg.contenttypes.domain.ComparisonAuthorInfo

data class ComparisonAuthorRepresentation(
    val author: AuthorRepresentation,
    val info: Iterable<ComparisonAuthorInfo>,
)
