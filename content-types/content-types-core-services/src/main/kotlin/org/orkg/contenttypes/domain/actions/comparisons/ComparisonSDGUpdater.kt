package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.SDGUpdater
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.graph.input.StatementUseCases

class ComparisonSDGUpdater(
    private val sdgUpdater: SDGUpdater
) : UpdateComparisonAction {
    constructor(statementService: StatementUseCases) : this(SDGUpdater(statementService))

    override operator fun invoke(command: UpdateComparisonCommand, state: UpdateComparisonState): UpdateComparisonState {
        if (command.sustainableDevelopmentGoals != null && command.sustainableDevelopmentGoals != state.comparison!!.sustainableDevelopmentGoals.map { it.id }.toSet()) {
            sdgUpdater.update(command.contributorId, command.sustainableDevelopmentGoals!!, command.comparisonId)
        }
        return state
    }
}
