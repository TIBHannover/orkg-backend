package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonContributionCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        command.contributions.forEach { contributionId ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.comparisonId!!,
                    predicateId = Predicates.comparesContribution,
                    objectId = contributionId
                )
            )
        }
        return state
    }
}
