package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.UpdateTemplateInstanceAction.State
import org.orkg.contenttypes.input.TemplateUseCases

class TemplateInstanceTemplateValidator(
    private val templateService: TemplateUseCases,
) : UpdateTemplateInstanceAction {
    override fun invoke(command: UpdateTemplateInstanceCommand, state: State): State =
        state.copy(
            template = templateService.findById(command.templateId)
                .orElseThrow { TemplateNotFound(command.templateId) }
        )
}
