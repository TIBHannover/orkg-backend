package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.output.ComparisonTableRepository

class ComparisonPublishableValidator(
    private val comparisonService: ComparisonUseCases,
    private val comparisonTableRepository: ComparisonTableRepository
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val comparison = comparisonService.findById(command.id)
            .orElseThrow { ComparisonNotFound(command.id) }
        if (comparison.published) {
            throw ComparisonAlreadyPublished(command.id)
        }
        if (comparison.contributions.distinct().size < 2) {
            throw RequiresAtLeastTwoContributions()
        }
        val table = comparisonTableRepository.findById(command.id)
            .orElseThrow { ComparisonNotFound(command.id) }
        return state.copy(comparison = comparison, config = table.config, data = table.data)
    }
}
