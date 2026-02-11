package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.CreateTemplatePropertyAction.State

class TemplatePropertyTemplateCreateValidator : CreateTemplatePropertyAction {
    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State {
        if (state.template!!.isClosed) {
            throw TemplateClosed(command.templateId)
        }
        return state
    }
}
