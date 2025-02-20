package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateSmartReviewAction : Action<CreateSmartReviewCommand, CreateSmartReviewAction.State> {
    data class State(
        val smartReviewId: ThingId? = null,
        val contributionId: ThingId? = null,
        val authors: List<Author> = emptyList(),
    )
}

interface UpdateSmartReviewAction : Action<UpdateSmartReviewCommand, UpdateSmartReviewAction.State> {
    data class State(
        val smartReview: SmartReview? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val authors: List<Author> = emptyList(),
    )
}

interface PublishSmartReviewAction : Action<PublishSmartReviewCommand, PublishSmartReviewAction.State> {
    data class State(
        val smartReview: SmartReview? = null,
        val smartReviewVersionId: ThingId? = null,
    )
}
