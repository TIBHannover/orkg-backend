package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId

data class ContributorRecord(
    val contributorId: ContributorId,
    val comparisonCount: Long = 0,
    val paperCount: Long = 0,
    val contributionCount: Long = 0,
    val researchProblemCount: Long = 0,
    val visualizationCount: Long = 0,
    val totalCount: Long = 0,
)
