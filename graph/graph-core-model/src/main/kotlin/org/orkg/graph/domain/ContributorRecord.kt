package org.orkg.graph.domain

import org.orkg.common.ContributorId

data class ContributorRecord(
    val contributor: ContributorId,
    val comparisons: Long = 0,
    val papers: Long = 0,
    val contributions: Long = 0,
    val problems: Long = 0,
    val visualizations: Long = 0,
    val total: Long = 0
)
