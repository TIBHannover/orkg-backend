package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonResearchFieldUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.researchFields != null && command.researchFields != state.comparison!!.researchFields.map { it.id }) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.comparisonId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.hasSubject,
                objects = command.researchFields!!
            )
        }
        return state
    }
}
