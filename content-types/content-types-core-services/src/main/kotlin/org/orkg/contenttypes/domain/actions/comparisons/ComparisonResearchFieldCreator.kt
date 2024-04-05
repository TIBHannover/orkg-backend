package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.ResearchFieldCreator
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class ComparisonResearchFieldCreator(
    statementService: StatementUseCases
) : ResearchFieldCreator(statementService), CreateComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State =
        state.apply { create(command.contributorId, command.researchFields, state.comparisonId!!, Predicates.hasSubject) }
}
