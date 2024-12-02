package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class SmartReviewResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateSmartReviewAction {
    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        resourceService.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.smartReviewId,
                label = command.title,
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state
    }
}
