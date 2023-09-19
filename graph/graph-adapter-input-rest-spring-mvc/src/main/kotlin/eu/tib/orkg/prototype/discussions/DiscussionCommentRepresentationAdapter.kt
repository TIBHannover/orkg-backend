package eu.tib.orkg.prototype.discussions

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.discussions.api.DiscussionCommentRepresentation
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface DiscussionCommentRepresentationAdapter {
    fun Optional<DiscussionComment>.mapToDiscussionCommentRepresentation(): Optional<DiscussionCommentRepresentation> =
        map { it.toDiscussionCommentRepresentation() }

    fun Page<DiscussionComment>.mapToDiscussionCommentRepresentation(): Page<DiscussionCommentRepresentation> =
        map { it.toDiscussionCommentRepresentation() }

    fun DiscussionComment.toDiscussionCommentRepresentation(): DiscussionCommentRepresentation =
        object : DiscussionCommentRepresentation {
            override val id: DiscussionCommentId = this@toDiscussionCommentRepresentation.id
            override val message: String = this@toDiscussionCommentRepresentation.message
            override val createdBy: ContributorId = this@toDiscussionCommentRepresentation.createdBy
            override val createdAt: OffsetDateTime = this@toDiscussionCommentRepresentation.createdAt
        }
}
