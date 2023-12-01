package org.orkg.contenttypes.domain.actions.visualization

import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction.State

class VisualizationObservatoryValidator(
    observatoryRepository: ObservatoryRepository
) : ObservatoryValidator(observatoryRepository), VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        validate(command.observatories)
        return state
    }
}
