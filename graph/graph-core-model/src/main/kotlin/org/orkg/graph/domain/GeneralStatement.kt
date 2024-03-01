package org.orkg.graph.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId

data class GeneralStatement(
    val id: StatementId? = null,
    val subject: Thing,
    val predicate: Predicate,
    val `object`: Thing,
    val createdAt: OffsetDateTime?,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
    val modifiable: Boolean = true,
    val index: Int? = null
) {
    fun isOwnedBy(contributorId: ContributorId) = createdBy == contributorId
}
