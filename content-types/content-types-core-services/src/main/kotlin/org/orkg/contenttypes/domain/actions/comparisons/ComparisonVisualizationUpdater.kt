package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class ComparisonVisualizationUpdater(
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
        if (command.visualizations != null && command.visualizations != state.comparison!!.visualizations) {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.hasVisualization,
                objects = command.visualizations!!.toSet()
            )
        }
        return state
    }
}
