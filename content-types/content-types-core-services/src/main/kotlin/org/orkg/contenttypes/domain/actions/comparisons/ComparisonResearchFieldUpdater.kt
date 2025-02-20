package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonResearchFieldUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateComparisonAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService, unsafeStatementUseCases))

    override operator fun invoke(
        command: UpdateComparisonCommand,
        state: UpdateComparisonState,
    ): UpdateComparisonState {
        if (command.researchFields != null && command.researchFields != state.comparison!!.researchFields.map { it.id }) {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.hasSubject,
                objects = command.researchFields!!
            )
        }
        return state
    }
}
