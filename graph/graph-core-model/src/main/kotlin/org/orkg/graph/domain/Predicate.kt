package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import java.time.OffsetDateTime

data class Predicate(
    override val id: ThingId,
    override val label: String,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId = ContributorId.UNKNOWN,
    override val modifiable: Boolean = true,
) : Thing {
    fun isOwnedBy(contributorId: ContributorId): Boolean = createdBy == contributorId
}
