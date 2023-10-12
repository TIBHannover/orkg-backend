package eu.tib.orkg.prototype.discussions

import eu.tib.orkg.prototype.discussions.api.DiscussionCommentRepresentation
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import java.util.*
import org.springframework.data.domain.Page

interface DiscussionCommentRepresentationAdapter {
    fun Optional<DiscussionComment>.mapToDiscussionCommentRepresentation(): Optional<DiscussionCommentRepresentation> =
        map { it.toDiscussionCommentRepresentation() }

    fun Page<DiscussionComment>.mapToDiscussionCommentRepresentation(): Page<DiscussionCommentRepresentation> =
        map { it.toDiscussionCommentRepresentation() }

    fun DiscussionComment.toDiscussionCommentRepresentation(): DiscussionCommentRepresentation =
        DiscussionCommentRepresentation(id, message, createdBy, createdAt)
}
