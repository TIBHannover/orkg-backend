package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateSmartReviewSectionAction : Action<CreateSmartReviewSectionCommand, CreateSmartReviewSectionAction.State> {
    data class State(
        val smartReviewSectionId: ThingId? = null,
        val contributionId: ThingId? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap()
    )
}
