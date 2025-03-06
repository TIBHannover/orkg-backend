package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class PaperResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val paperId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.title,
                classes = setOf(Classes.paper),
                extractionMethod = command.extractionMethod,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull()
            )
        )
        return state.copy(paperId = paperId)
    }
}
