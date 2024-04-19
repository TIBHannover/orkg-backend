package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class LiteratureListResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateLiteratureListAction {
    override operator fun invoke(
        command: UpdateLiteratureListCommand,
        state: UpdateLiteratureListState
    ): UpdateLiteratureListState {
        resourceService.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.literatureListId,
                label = command.title,
                observatoryId = command.observatories?.singleOrNull(),
                organizationId = command.organizations?.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state
    }
}
