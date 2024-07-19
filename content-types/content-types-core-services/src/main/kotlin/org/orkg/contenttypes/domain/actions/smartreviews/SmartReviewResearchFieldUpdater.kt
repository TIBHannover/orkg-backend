package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewResearchFieldUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) : UpdateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService))

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.researchFields != null && command.researchFields != state.smartReview!!.researchFields.ids) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.smartReviewId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.smartReviewId,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields!!.toSet()
            )
        }
        return state
    }
}
