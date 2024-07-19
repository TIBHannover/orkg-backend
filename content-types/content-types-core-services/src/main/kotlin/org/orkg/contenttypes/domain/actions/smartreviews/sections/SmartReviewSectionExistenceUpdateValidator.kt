package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.contenttypes.domain.SmartReviewService
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewExistenceValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.UpdateSmartReviewSectionAction.State
import org.orkg.graph.output.ResourceRepository

class SmartReviewSectionExistenceUpdateValidator(
    private val abstractSmartReviewExistenceValidator: AbstractSmartReviewExistenceValidator
) : UpdateSmartReviewSectionAction {
    constructor(
        SmartReviewService: SmartReviewService,
        resourceRepository: ResourceRepository
    ) : this(AbstractSmartReviewExistenceValidator(SmartReviewService, resourceRepository))

    override fun invoke(command: UpdateSmartReviewSectionCommand, state: State): State =
        abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(command.smartReviewId)
            .let { state.copy(smartReview = it.first, statements = it.second) }
}
