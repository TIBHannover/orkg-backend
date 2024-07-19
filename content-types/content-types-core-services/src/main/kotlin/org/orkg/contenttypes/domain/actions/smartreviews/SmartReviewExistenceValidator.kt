package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.SmartReviewService
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.output.ResourceRepository

class SmartReviewExistenceValidator(
    private val abstractSmartReviewExistenceValidator: AbstractSmartReviewExistenceValidator
) : UpdateSmartReviewAction {
    constructor(
        smartReviewService: SmartReviewService,
        resourceRepository: ResourceRepository
    ) : this(AbstractSmartReviewExistenceValidator(smartReviewService, resourceRepository))

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State =
        abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(command.smartReviewId)
            .let { state.copy(smartReview = it.first, statements = it.second) }
}
