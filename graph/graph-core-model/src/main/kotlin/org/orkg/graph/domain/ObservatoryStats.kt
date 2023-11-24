package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ObservatoryId

/**
 * Data class for fetching
 * Observatory resources
 */
data class ObservatoryStats(
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    val papers: Long = 0,
    val comparisons: Long = 0,
    val total: Long = 0
)
