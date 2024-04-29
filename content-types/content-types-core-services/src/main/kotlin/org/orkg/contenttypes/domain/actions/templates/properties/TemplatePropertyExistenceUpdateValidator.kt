package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.UpdateTemplatePropertyAction.State
import org.orkg.contenttypes.input.TemplateUseCases

class TemplatePropertyExistenceUpdateValidator(
    private val templateService: TemplateUseCases
) : UpdateTemplatePropertyAction {
    override fun invoke(command: UpdateTemplatePropertyCommand, state: State): State {
        val template = templateService.findById(command.templateId)
            .orElseThrow { TemplateNotFound(command.templateId) }
        return state.copy(template = template)
    }
}
