package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.input.TemplateUseCases

class TemplateExistenceValidator(
    private val templateService: TemplateUseCases
) : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: UpdateTemplateState): UpdateTemplateState {
        val template = templateService.findById(command.templateId)
            .orElseThrow { TemplateNotFound(command.templateId) }
        return state.copy(template = template)
    }
}
