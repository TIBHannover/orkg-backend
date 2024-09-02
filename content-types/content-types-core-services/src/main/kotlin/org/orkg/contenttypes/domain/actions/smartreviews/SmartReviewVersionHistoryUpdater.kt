package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class SmartReviewVersionHistoryUpdater(
    private val statementService: StatementUseCases
) : PublishSmartReviewAction {
    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        statementService.add(
            userId = command.contributorId,
            subject = state.smartReview!!.id,
            predicate = Predicates.hasPublishedVersion,
            `object` = state.smartReviewVersionId!!
        )
        return state
    }
}
