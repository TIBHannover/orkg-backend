package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

data class List(
    val id: ThingId,
    val label: String,
    val elements: kotlin.collections.List<ThingId>,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
    val modifiable: Boolean = true
)
