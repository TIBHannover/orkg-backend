package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.UnrelatedTemplateProperty
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.UpdateTemplatePropertyAction.State

class TemplatePropertyTemplateUpdateValidator : UpdateTemplatePropertyAction {
    override fun invoke(command: UpdateTemplatePropertyCommand, state: State): State {
        if (state.template!!.isClosed) {
            throw TemplateClosed(command.templateId)
        }
        return state.copy(
            templateProperty = state.template.properties.find { it.id == command.templatePropertyId }
                ?: throw UnrelatedTemplateProperty(command.templateId, command.templatePropertyId)
        )
    }
}
