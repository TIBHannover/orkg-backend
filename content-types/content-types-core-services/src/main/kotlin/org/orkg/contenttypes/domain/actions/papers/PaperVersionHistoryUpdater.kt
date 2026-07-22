package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.PublishPaperCommand
import org.orkg.contenttypes.domain.actions.papers.PublishPaperAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class PaperVersionHistoryUpdater(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : PublishPaperAction {
    override fun invoke(command: PublishPaperCommand, state: State): State {
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.paper!!.id,
                predicateId = Predicates.hasPublishedVersion,
                objectId = state.paperVersionId!!,
            ),
        )
        state.paper.versions.published.firstOrNull()?.let { latestVersion ->
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = latestVersion.id,
                    contributorId = command.contributorId,
                    classes = setOf(Classes.paperVersion),
                ),
            )
        }
        return state
    }
}
