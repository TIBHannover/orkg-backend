package org.orkg.graph.domain

import org.orkg.common.ThingId

/**
 * Data class for fetching research field stats
 */
data class ResearchFieldStats(
    val id: ThingId,
    val papers: Long = 0,
    val comparisons: Long = 0,
    val total: Long = 0,
)
