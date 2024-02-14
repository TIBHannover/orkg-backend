package org.orkg.contenttypes.domain.actions.visualization

import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class VisualizationResourceCreator(
    private val resourceService: ResourceUseCases
) : VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        val visualizationId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.visualization),
                extractionMethod = command.extractionMethod,
                contributorId = command.contributorId,
                observatoryId = command.observatories.firstOrNull(),
                organizationId = command.organizations.firstOrNull()
            )
        )
        return state.copy(visualizationId = visualizationId)
    }
}
