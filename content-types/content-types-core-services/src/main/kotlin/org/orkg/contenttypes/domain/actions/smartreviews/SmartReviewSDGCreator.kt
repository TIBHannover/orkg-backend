package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewSDGCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator
) : CreateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyCreator(literalService, statementService))

    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        statementCollectionPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.smartReviewId!!,
            predicateId = Predicates.sustainableDevelopmentGoal,
            objects = command.sustainableDevelopmentGoals.toList()
        )
        return state
    }
}
