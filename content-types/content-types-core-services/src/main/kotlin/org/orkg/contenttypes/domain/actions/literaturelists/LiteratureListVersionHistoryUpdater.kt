package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases

class LiteratureListVersionHistoryUpdater(
    private val statementService: StatementUseCases
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        statementService.add(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.literatureList!!.id,
                predicateId = Predicates.hasPublishedVersion,
                objectId = state.literatureListVersionId!!
            )
        )
        return state
    }
}
