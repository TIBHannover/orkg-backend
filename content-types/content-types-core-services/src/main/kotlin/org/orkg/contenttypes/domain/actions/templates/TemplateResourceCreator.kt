package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases

class TemplateResourceCreator(
    private val resourceService: ResourceUseCases
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        val templateId = resourceService.createUnsafe(
            CreateResourceUseCase.CreateCommand(
                label = command.label,
                classes = setOf(Classes.nodeShape),
                contributorId = command.contributorId,
                observatoryId = command.observatories.firstOrNull(),
                organizationId = command.organizations.firstOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state.copy(templateId = templateId)
    }
}
