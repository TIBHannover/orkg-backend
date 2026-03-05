package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonContributionUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateComparisonAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementuseCaes: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementuseCaes, unsafeStatementUseCases),
    )

    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.sources != null && command.sources != state.comparison!!.sources) {
            statementCollectionPropertyUpdater.update(
                statements = state.statements[command.comparisonId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicates = setOf(Predicates.comparesContribution, Predicates.comparesRosettaStoneContribution),
                objects = command.sources!!,
                predicateSelector = { it.type.predicateId },
                objectIdSelector = { it.id },
            )
        }
        return state
    }
}
