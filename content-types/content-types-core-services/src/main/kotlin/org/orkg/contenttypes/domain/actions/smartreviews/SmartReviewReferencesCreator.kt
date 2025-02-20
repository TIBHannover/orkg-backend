package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewReferencesCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator,
) : CreateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyCreator(literalService, unsafeStatementUseCases))

    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        statementCollectionPropertyCreator.create(
            contributorId = command.contributorId,
            subjectId = state.contributionId!!,
            predicateId = Predicates.hasReference,
            labels = command.references
        )
        return state
    }
}
