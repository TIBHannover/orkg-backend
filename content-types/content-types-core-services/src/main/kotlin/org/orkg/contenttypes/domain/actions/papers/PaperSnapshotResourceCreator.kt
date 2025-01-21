package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class PaperSnapshotResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases
) : CreatePaperAction {
    override fun invoke(command: CreatePaperCommand, state: CreatePaperAction.State): CreatePaperAction.State {
        val paperId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.paperVersion),
                extractionMethod = command.extractionMethod,
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull()
            )
        )
        return state.copy(paperId = paperId)
    }
}
