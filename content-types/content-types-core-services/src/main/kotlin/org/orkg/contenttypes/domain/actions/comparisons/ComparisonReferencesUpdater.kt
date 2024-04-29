package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class ComparisonReferencesUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) : UpdateComparisonAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyUpdater(literalService, statementService))

    override operator fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.references != null && command.references!!.toSet() != state.comparison!!.references.toSet()) {
            statementCollectionPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.comparisonId,
                predicateId = Predicates.reference,
                literals = command.references!!.toSet()
            )
        }
        return state
    }
}
