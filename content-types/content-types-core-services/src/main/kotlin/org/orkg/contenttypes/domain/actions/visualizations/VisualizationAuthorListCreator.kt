package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.AbstractAuthorListCreator
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class VisualizationAuthorListCreator(
    private val authorCreator: AbstractAuthorListCreator,
) : VisualizationAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
    ) : this(
        AbstractAuthorListCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService),
    )

    override fun invoke(command: CreateVisualizationCommand, state: State): State =
        state.apply { authorCreator.create(command.contributorId, authors, visualizationId!!) }
}
