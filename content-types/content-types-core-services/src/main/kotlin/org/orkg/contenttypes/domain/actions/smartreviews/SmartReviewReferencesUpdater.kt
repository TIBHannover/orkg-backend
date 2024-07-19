package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewReferencesUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) : UpdateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService))

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.references != null && command.references != state.smartReview!!.references) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.smartReviewId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasReference,
                literals = command.references!!
            )
        }
        return state
    }
}
