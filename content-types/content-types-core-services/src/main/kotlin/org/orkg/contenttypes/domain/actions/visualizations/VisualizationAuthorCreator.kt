package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.AuthorCreator
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class VisualizationAuthorCreator(
    unsafeResourceUseCases: UnsafeResourceUseCases,
    unsafeStatementUseCases: UnsafeStatementUseCases,
    literalService: LiteralUseCases,
    listService: ListUseCases
) : AuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, literalService, listService), VisualizationAction {
    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        create(command.contributorId, state.authors, state.visualizationId!!)
        return state
    }
}
