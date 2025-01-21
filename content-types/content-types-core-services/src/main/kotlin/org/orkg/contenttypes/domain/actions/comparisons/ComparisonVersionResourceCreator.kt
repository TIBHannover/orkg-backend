package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class ComparisonVersionResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases
) : CreateComparisonAction {
    override fun invoke(command: CreateComparisonCommand, state: State): State {
        val comparisonId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.comparisonPublished, Classes.latestVersion),
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state.copy(comparisonId = comparisonId)
    }
}
