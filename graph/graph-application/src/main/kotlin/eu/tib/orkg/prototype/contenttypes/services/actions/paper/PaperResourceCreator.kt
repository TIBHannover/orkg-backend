package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ResourceUseCases

class PaperResourceCreator(
    private val resourceService: ResourceUseCases
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val paperId = resourceService.create(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.paper),
                extractionMethod = command.extractionMethod,
                contributorId = command.contributorId,
                observatoryId = command.observatories.firstOrNull(),
                organizationId = command.organizations.firstOrNull()
            )
        )
        return state.copy(paperId = paperId)
    }
}
