package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class PaperResourceCreator(
    private val resourceService: ResourceUseCases
) : CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val paperId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.paper),
                extractionMethod = command.extractionMethod,
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull()
            )
        )
        return state.copy(paperId = paperId)
    }
}
