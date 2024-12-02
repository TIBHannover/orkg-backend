package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.UpdateLiteratureListAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class LiteratureListResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateLiteratureListAction {
    override fun invoke(command: UpdateLiteratureListCommand, state: State): State {
        resourceService.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.literatureListId,
                label = command.title,
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state
    }
}
