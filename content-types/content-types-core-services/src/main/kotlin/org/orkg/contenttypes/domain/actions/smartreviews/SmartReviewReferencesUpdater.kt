package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewReferencesUpdater(
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
        if (command.references != null && command.references != state.smartReview!!.references) {
            val contributionId = state.statements.findContributionId(command.smartReviewId)
            if (contributionId != null) {
                statementCollectionPropertyUpdater.update(
                    statements = state.statements[contributionId].orEmpty(),
                    contributorId = command.contributorId,
                    subjectId = contributionId,
                    predicateId = Predicates.hasReference,
                    literals = command.references!!
                )
            }
        }
        return state
    }
}
