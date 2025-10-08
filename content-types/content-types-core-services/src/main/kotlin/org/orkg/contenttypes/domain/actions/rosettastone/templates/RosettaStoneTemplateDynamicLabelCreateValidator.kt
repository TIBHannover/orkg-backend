package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.MissingDynamicLabelPlaceholder
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction.State

class RosettaStoneTemplateDynamicLabelCreateValidator : CreateRosettaStoneTemplateAction {
    override fun invoke(command: CreateRosettaStoneTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, _ ->
            if (!command.dynamicLabel.template.contains("{$index}")) {
                throw MissingDynamicLabelPlaceholder(index)
            }
        }
        return state
    }
}
