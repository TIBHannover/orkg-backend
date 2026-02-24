package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.output.ComparisonTableRepository

class ComparisonVersionTableCreator(
    private val comparisonTableUseCases: ComparisonTableUseCases,
    private val comparisonTableRepository: ComparisonTableRepository,
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val table = comparisonTableUseCases.findByComparisonId(command.id)
            .map { it.copy(comparisonId = state.comparisonVersionId!!) }
            .orElseGet { ComparisonTable(state.comparisonVersionId!!) }
        comparisonTableRepository.save(table)
        return state
    }
}
