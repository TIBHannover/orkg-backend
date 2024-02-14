package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class ComparisonResourceCreator(
    private val resourceService: ResourceUseCases
) : ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        val comparisonId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.comparison),
                extractionMethod = command.extractionMethod,
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull()
            )
        )
        return state.copy(comparisonId = comparisonId)
    }
}
