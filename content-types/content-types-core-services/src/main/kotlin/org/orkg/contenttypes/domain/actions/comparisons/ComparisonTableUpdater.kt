package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.contenttypes.output.ComparisonTableRepository

class ComparisonTableUpdater(
    private val comparisonTableRepository: ComparisonTableRepository
) : UpdateComparisonAction {
    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (command.config != null && command.config != state.comparison!!.config || command.data != null && command.data != state.comparison!!.data) {
            comparisonTableRepository.update(
                ComparisonTable(
                    id = state.comparison.id,
                    config = command.config ?: state.comparison.config,
                    data = command.data ?: state.comparison.data
                )
            )
        }
        return state
    }
}
