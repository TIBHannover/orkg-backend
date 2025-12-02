package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State

class TemplateClosedValidator : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (state.template!!.isClosed && command.hasPropertyChanges(state.template.properties)) {
            throw TemplateClosed(command.templateId)
        }
        return state
    }

    private fun UpdateTemplateCommand.hasPropertyChanges(existingProperties: List<TemplateProperty>): Boolean {
        if (properties == null) {
            return false
        }
        if (properties!!.size != existingProperties.size) {
            return true
        }
        properties!!.forEachIndexed { index, property ->
            if (!property.matchesProperty(existingProperties[index])) {
                return true
            }
        }
        return false
    }
}
