package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.ResearchFieldCreator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State
import org.orkg.graph.input.StatementUseCases

class ComparisonResearchFieldCreator(
    statementService: StatementUseCases
) : ResearchFieldCreator(statementService), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        create(command.contributorId, command.researchFields, state.comparisonId!!)
        return state
    }
}
