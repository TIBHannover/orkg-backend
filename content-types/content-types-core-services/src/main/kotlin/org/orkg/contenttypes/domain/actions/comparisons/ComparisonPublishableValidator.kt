package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoSources
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.input.ComparisonUseCases

class ComparisonPublishableValidator(
    private val comparisonService: ComparisonUseCases,
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val comparison = comparisonService.findById(command.id)
            .orElseThrow { ComparisonNotFound(command.id) }
        if (comparison.published) {
            throw ComparisonAlreadyPublished(command.id)
        }
        if (comparison.sources.distinct().size < 2) {
            throw RequiresAtLeastTwoSources()
        }
        return state.copy(comparison = comparison)
    }
}
