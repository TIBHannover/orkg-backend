package eu.tib.orkg.prototype.discussions.domain.model

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime

data class DiscussionComment(
    val id: DiscussionCommentId,
    val topic: ThingId,
    val message: String,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime
) {
    fun isOwnedBy(contributor: ContributorId): Boolean = createdBy == contributor
}
