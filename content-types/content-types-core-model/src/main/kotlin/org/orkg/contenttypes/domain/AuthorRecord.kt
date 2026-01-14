package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class AuthorRecord(
    val authorId: ThingId?,
    val authorName: String,
    val comparisonCount: Long = 0,
    val paperCount: Long = 0,
    val visualizationCount: Long = 0,
    val totalCount: Long = 0,
)
