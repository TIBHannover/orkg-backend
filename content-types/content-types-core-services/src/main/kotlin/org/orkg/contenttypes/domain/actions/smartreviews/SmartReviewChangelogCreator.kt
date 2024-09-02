package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewChangelogCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator
) : PublishSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
    ) : this(SingleStatementPropertyCreator(literalService, statementService))

    override fun invoke(command: PublishSmartReviewCommand, state: State): State {
        singleStatementPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.smartReviewVersionId!!,
            predicateId = Predicates.description,
            label = command.changelog
        )
        return state
    }
}
