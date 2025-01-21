package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class ComparisonResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases
) : CreateComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        val comparisonId = unsafeResourceUseCases.create(
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
