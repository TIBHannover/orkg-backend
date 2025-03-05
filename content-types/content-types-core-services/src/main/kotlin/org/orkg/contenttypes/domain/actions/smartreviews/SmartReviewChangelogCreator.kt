package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.PublishSmartReviewCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.smartreviews.PublishSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewChangelogCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
) : PublishSmartReviewAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases))

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
