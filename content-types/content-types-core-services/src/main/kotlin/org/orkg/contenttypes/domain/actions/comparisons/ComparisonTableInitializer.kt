package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.UpdateComparisonTableUseCase
import org.orkg.contenttypes.output.ComparisonTableRepository

class ComparisonTableInitializer(
    private val comparisonTableUseCases: ComparisonTableUseCases,
    private val comparisonTableRepository: ComparisonTableRepository,
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        val selectedPaths = comparisonTableUseCases.findAllPathsByComparisonId(state.comparisonId!!)
        comparisonTableRepository.save(ComparisonTable(state.comparisonId, selectedPaths))
        return state
    }
}
