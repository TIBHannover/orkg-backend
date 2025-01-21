package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class VisualizationAuthorCreator(
    unsafeResourceUseCases: UnsafeResourceUseCases,
    statementService: StatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorCreator(unsafeResourceUseCases, statementService, literalService, listService), VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        create(command.contributorId, state.authors, state.visualizationId!!)
        return state
    }
}
