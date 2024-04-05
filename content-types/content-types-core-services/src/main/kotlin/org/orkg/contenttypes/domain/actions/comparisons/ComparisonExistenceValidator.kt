package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.input.ComparisonUseCases

class ComparisonExistenceValidator(
    private val comparisonService: ComparisonUseCases
) : UpdateComparisonAction {
    override fun invoke(command: UpdateComparisonCommand, state: UpdateComparisonState): UpdateComparisonState {
        val comparison = comparisonService.findById(command.comparisonId)
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        return state.copy(comparison = comparison)
    }
}
