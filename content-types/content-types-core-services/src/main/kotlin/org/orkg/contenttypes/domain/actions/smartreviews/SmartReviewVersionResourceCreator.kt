package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class SmartReviewVersionResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : CreateSmartReviewAction {
    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        val smartReviewId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.smartReviewPublished),
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state.copy(smartReviewId = smartReviewId)
    }
}
