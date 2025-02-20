package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewReferencesUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService, unsafeStatementUseCases))

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        if (command.references != null && command.references != state.smartReview!!.references) {
            val directStatements = state.statements[command.smartReviewId].orEmpty()
            val contributionId = directStatements.singleOrNull {
                it.predicate.id == Predicates.hasContribution &&
                    it.`object` is Resource &&
                    Classes.contributionSmartReview in (it.`object` as Resource).classes
            }?.`object`?.id
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
