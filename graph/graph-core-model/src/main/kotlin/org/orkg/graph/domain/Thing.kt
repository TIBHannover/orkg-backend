package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

sealed interface Thing {
    val id: ThingId
    val label: String
    val createdAt: OffsetDateTime
    val createdBy: ContributorId
    val modifiable: Boolean
}
