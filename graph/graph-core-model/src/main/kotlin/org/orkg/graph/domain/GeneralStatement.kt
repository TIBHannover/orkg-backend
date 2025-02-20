package org.orkg.graph.domain

import org.orkg.common.ContributorId
import java.time.OffsetDateTime

data class GeneralStatement(
    val id: StatementId,
    val subject: Thing,
    val predicate: Predicate,
    val `object`: Thing,
    val createdAt: OffsetDateTime?,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
    val modifiable: Boolean = true,
    val index: Int? = null,
) {
    fun isOwnedBy(contributorId: ContributorId) = createdBy == contributorId
}
