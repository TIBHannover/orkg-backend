package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.SmartReviewAlreadyPublished
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.contenttypes.input.SmartReviewUseCases

class SmartReviewPublishableValidator(
    private val smartReviewService: SmartReviewUseCases
) : PublishSmartReviewAction {
    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        val smartReview = smartReviewService.findById(command.smartReviewId)
            .orElseThrow { SmartReviewNotFound(command.smartReviewId) }
        if (smartReview.published) {
            throw SmartReviewAlreadyPublished(command.smartReviewId)
        }
        return state.copy(smartReview = smartReview)
    }
}
