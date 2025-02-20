package org.orkg.graph.domain

import org.orkg.common.ThingId

/**
 * Data class comprising resource ID,
 * research problem and total number of
 * papers per research problem
 */
data class TrendingResearchProblems(
    val id: ThingId,
    val researchProblem: String,
    val papersCount: Long,
)
