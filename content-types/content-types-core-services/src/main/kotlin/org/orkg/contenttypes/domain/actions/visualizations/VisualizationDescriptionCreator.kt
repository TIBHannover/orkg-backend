package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class VisualizationDescriptionCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator
) : VisualizationAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases
    ) : this(SingleStatementPropertyCreator(literalService, unsafeStatementUseCases))

    override operator fun invoke(command: CreateVisualizationCommand, state: State): State =
        state.apply {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.visualizationId!!,
                predicateId = Predicates.description,
                label = command.description
            )
        }
}
