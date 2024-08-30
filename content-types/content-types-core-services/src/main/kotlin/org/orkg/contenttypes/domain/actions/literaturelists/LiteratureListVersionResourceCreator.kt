package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class LiteratureListVersionResourceCreator(
    private val resourceService: ResourceUseCases
) : CreateLiteratureListAction {
    override fun invoke(command: CreateLiteratureListCommand, state: State): State {
        val literatureListId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = command.title,
                classes = setOf(Classes.literatureListPublished),
                contributorId = command.contributorId,
                observatoryId = command.observatories.singleOrNull(),
                organizationId = command.organizations.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state.copy(literatureListId = literatureListId)
    }
}
