package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.contenttypes.domain.SmartReviewService
import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewExistenceValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.DeleteSmartReviewSectionAction.State
import org.orkg.graph.output.ResourceRepository

class SmartReviewSectionExistenceDeleteValidator(
    private val abstractSmartReviewExistenceValidator: AbstractSmartReviewExistenceValidator,
) : DeleteSmartReviewSectionAction {
    constructor(
        smartReviewService: SmartReviewService,
        resourceRepository: ResourceRepository,
    ) : this(AbstractSmartReviewExistenceValidator(smartReviewService, resourceRepository))

    override fun invoke(command: DeleteSmartReviewSectionCommand, state: State): State =
        abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(command.smartReviewId)
            .let { state.copy(smartReview = it.first, statements = it.second) }
}
