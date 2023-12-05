package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.DescriptionCreator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonDescriptionCreator(
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : DescriptionCreator(literalService, statementService), ComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State =
        state.apply { create(command.contributorId, state.comparisonId!!, command.description) }
}
