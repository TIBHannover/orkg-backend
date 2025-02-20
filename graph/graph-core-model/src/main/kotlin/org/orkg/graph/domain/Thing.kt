package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import java.time.OffsetDateTime

sealed interface Thing {
    val id: ThingId
    val label: String
    val createdAt: OffsetDateTime
    val createdBy: ContributorId
    val modifiable: Boolean
}
