package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

data class Predicate(
    override val id: ThingId,
    override val label: String,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId = ContributorId.UNKNOWN,
    val description: String? = null,
    override val modifiable: Boolean = true
) : Thing {
    fun isOwnedBy(contributorId: ContributorId): Boolean = createdBy == contributorId
}
