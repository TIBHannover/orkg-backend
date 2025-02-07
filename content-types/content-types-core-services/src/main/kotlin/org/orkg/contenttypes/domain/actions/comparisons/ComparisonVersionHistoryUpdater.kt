package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonVersionHistoryUpdater(
    private val statementService: StatementUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        statementService.add(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.comparison!!.id,
                predicateId = Predicates.hasPublishedVersion,
                objectId = state.comparisonVersionId!!
            )
        )
        state.comparison.versions.published.firstOrNull()?.let { latestVersion ->
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = latestVersion.id,
                    contributorId = command.contributorId,
                    classes = setOf(Classes.comparisonPublished)
                )
            )
        }
        return state
    }
}
