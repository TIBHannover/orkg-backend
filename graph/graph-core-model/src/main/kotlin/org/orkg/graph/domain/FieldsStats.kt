package org.orkg.graph.domain

import org.orkg.common.ThingId

/**
 * Data class for fetching
 * field statistics
 */
data class FieldsStats(
    val fieldId: ThingId,
    val field: String,
    val papers: Long,
)
