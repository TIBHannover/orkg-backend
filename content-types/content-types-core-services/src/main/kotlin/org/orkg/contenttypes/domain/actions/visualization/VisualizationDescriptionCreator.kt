package org.orkg.contenttypes.domain.actions.visualization

import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.DescriptionCreator
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction

class VisualizationDescriptionCreator(
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : DescriptionCreator(literalService, statementService), VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        create(command.contributorId, state.visualizationId!!, command.description)
        return state
    }
}
