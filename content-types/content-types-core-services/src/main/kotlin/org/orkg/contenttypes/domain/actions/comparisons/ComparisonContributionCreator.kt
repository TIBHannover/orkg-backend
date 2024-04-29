package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class ComparisonContributionCreator(
    val statementService: StatementUseCases
) : CreateComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        command.contributions.forEach { contributionId ->
            statementService.add(
                userId = command.contributorId,
                subject = state.comparisonId!!,
                predicate = Predicates.comparesContribution,
                `object` = contributionId
            )
        }
        return state
    }
}
