package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonDescriptionUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) : UpdateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases))

    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.description != null && command.description != state.comparison!!.description) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.description,
                label = command.description!!
            )
        }
        return state
    }
}
