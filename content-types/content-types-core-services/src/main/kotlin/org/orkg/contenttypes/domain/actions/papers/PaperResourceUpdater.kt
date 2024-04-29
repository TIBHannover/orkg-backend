package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class PaperResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState =
        state.apply {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = command.paperId,
                    label = command.title,
                    observatoryId = command.observatories?.singleOrNull(),
                    organizationId = command.organizations?.singleOrNull()
                )
            )
        }
}
