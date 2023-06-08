package eu.tib.orkg.prototype.discussions.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

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
