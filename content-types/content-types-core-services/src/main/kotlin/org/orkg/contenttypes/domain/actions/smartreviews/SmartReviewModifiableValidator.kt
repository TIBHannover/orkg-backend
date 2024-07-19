package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State

class SmartReviewModifiableValidator : UpdateSmartReviewAction {
    override fun invoke(command: UpdateSmartReviewCommand, state: State): State =
        state.apply {
            if (smartReview!!.published) {
                throw SmartReviewNotModifiable(command.smartReviewId)
            }
        }
}
