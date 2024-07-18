package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand

interface CreateSmartReviewAction : Action<CreateSmartReviewCommand, CreateSmartReviewAction.State> {
    data class State(
        val smartReviewId: ThingId? = null,
        val contributionId: ThingId? = null,
        val authors: List<Author> = emptyList()
    )
}
