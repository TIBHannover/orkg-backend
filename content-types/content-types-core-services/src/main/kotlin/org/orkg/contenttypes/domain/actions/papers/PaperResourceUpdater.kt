package org.orkg.contenttypes.domain.actions.papers

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class PaperResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdatePaperAction {
    override fun invoke(command: UpdatePaperCommand, state: State): State {
        resourceService.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.paperId,
                label = command.title,
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull()
            )
        )
        return state
    }
}
