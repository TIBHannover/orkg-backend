package org.orkg.graph.domain

import org.orkg.common.ContributorId
import java.time.OffsetDateTime

data class SubgraphContribution(
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
)
