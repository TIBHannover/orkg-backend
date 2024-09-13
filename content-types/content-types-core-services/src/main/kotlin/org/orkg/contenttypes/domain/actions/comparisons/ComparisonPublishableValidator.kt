package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.PublishComparisonAction.State
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases

class ComparisonPublishableValidator(
    private val resourceService: ResourceUseCases,
    private val comparisonPublishedRepository: ComparisonPublishedRepository
) : PublishComparisonAction {
    override fun invoke(command: PublishComparisonCommand, state: State): State {
        val comparison = resourceService.findById(command.id)
            .filter { Classes.comparison in it.classes }
            .orElseThrow { ComparisonNotFound(command.id) }
        comparisonPublishedRepository.findById(command.id)
            .ifPresent { throw ComparisonAlreadyPublished(command.id) }
        return state.copy(comparison = comparison)
    }
}
