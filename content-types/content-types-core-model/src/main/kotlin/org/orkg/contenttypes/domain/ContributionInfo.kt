package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class ContributionInfo(
    val id: ThingId,
    val label: String,
    val paperTitle: String,
    val paperYear: Int?,
    val paperId: ThingId,
)
