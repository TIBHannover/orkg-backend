package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class LiteratureListVersionHistoryUpdater(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.literatureList!!.id,
                predicateId = Predicates.hasPublishedVersion,
                objectId = state.literatureListVersionId!!
            )
        )
        state.literatureList.versions.published.firstOrNull()?.let { latestVersion ->
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = latestVersion.id,
                    contributorId = command.contributorId,
                    classes = setOf(Classes.literatureListPublished)
                )
            )
        }
        return state
    }
}
