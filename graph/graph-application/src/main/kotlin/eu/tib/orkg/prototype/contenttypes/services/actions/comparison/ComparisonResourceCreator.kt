package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ResourceUseCases

class ComparisonResourceCreator(
    private val resourceService: ResourceUseCases
) : ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        val comparisonId = resourceService.create(
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
