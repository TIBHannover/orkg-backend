package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.CreateTemplateInstanceAction.State
import org.orkg.contenttypes.input.TemplateUseCases

class TemplateInstanceTemplateCreateValidator(
    private val templateService: TemplateUseCases,
) : CreateTemplateInstanceAction {
    override fun invoke(command: CreateTemplateInstanceCommand, state: State): State =
        state.copy(
            template = templateService.findById(command.templateId)
                .orElseThrow { TemplateNotFound(command.templateId) },
        )
}
