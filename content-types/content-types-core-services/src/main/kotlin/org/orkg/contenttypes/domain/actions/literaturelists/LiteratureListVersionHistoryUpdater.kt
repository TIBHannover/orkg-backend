package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class LiteratureListVersionHistoryUpdater(
    private val statementService: StatementUseCases
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        statementService.add(
            userId = command.contributorId,
            subject = state.literatureList!!.id,
            predicate = Predicates.hasPublishedVersion,
            `object` = state.literatureListVersionId!!
        )
        return state
    }
}
