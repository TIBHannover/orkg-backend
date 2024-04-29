package org.orkg.discussions.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

data class DiscussionComment(
    val id: DiscussionCommentId,
    val topic: ThingId,
    val message: String,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime
) {
    fun isOwnedBy(contributor: ContributorId): Boolean = createdBy == contributor
}
