package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class LiteratureListVersionResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : CreateLiteratureListAction {
    override fun invoke(command: CreateLiteratureListCommand, state: State): State {
        val literatureListId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.title,
                classes = setOf(Classes.literatureListPublished),
                extractionMethod = command.extractionMethod,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull()
            )
        )
        return state.copy(literatureListId = literatureListId)
    }
}
