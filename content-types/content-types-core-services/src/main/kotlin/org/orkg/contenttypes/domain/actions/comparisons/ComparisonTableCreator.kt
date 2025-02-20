package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.contenttypes.output.ComparisonTableRepository

class ComparisonTableCreator(
    private val comparisonTableRepository: ComparisonTableRepository,
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        comparisonTableRepository.save(ComparisonTable(state.comparisonId!!, command.config, command.data))
        return state
    }
}
