package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class VisualizationDescriptionCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
) : VisualizationAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases)
    )

    override operator fun invoke(command: CreateVisualizationCommand, state: State): State {
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.visualizationId!!,
            predicateId = Predicates.description,
            label = command.description
        )
        return state
    }
}
