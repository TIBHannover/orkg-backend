package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class LiteratureListResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases
) : CreateLiteratureListAction {
    override operator fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState
    ): CreateLiteratureListState {
        val literatureListId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.literatureList),
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state.copy(literatureListId = literatureListId)
    }
}
