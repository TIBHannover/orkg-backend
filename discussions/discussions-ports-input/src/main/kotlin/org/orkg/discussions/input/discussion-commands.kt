package org.orkg.discussions.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.discussions.domain.DiscussionCommentId

interface CreateDiscussionCommentUseCase {
    fun create(command: CreateCommand): DiscussionCommentId

    data class CreateCommand(
        val topic: ThingId,
        val message: String,
        val createdBy: ContributorId,
    )
}

interface DeleteDiscussionCommentUseCase {
    fun delete(contributorId: ContributorId, topic: ThingId, id: DiscussionCommentId)
}
