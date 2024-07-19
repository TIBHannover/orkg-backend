package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateSmartReviewSectionAction : Action<CreateSmartReviewSectionCommand, CreateSmartReviewSectionAction.State> {
    data class State(
        val smartReviewSectionId: ThingId? = null,
        val contributionId: ThingId? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap()
    )
}

interface UpdateSmartReviewSectionAction : Action<UpdateSmartReviewSectionCommand, UpdateSmartReviewSectionAction.State> {
    data class State(
        val smartReview: SmartReview? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}

interface DeleteSmartReviewSectionAction : Action<DeleteSmartReviewSectionCommand, DeleteSmartReviewSectionAction.State> {
    data class State(
        val smartReview: SmartReview? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}
