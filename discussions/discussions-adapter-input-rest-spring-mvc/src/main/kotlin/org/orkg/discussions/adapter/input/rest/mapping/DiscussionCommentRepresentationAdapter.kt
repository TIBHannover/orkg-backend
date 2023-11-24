package org.orkg.discussions.adapter.input.rest.mapping

import java.util.*
import org.orkg.discussions.adapter.input.rest.DiscussionCommentRepresentation
import org.orkg.discussions.domain.DiscussionComment
import org.springframework.data.domain.Page

interface DiscussionCommentRepresentationAdapter {
    fun Optional<DiscussionComment>.mapToDiscussionCommentRepresentation(): Optional<DiscussionCommentRepresentation> =
        map { it.toDiscussionCommentRepresentation() }

    fun Page<DiscussionComment>.mapToDiscussionCommentRepresentation(): Page<DiscussionCommentRepresentation> =
        map { it.toDiscussionCommentRepresentation() }

    fun DiscussionComment.toDiscussionCommentRepresentation(): DiscussionCommentRepresentation =
        DiscussionCommentRepresentation(id, message, createdBy, createdAt)
}
