package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewSDGUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateSmartReviewAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.sustainableDevelopmentGoals != null && command.sustainableDevelopmentGoals != state.smartReview!!.sustainableDevelopmentGoals.ids) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.smartReviewId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.sustainableDevelopmentGoal,
                objects = command.sustainableDevelopmentGoals!!,
            )
        }
        return state
    }
}
