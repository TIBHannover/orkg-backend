package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction.State

class RosettaStoneTemplateFormattedLabelCreateValidator : CreateRosettaStoneTemplateAction {
    override fun invoke(command: CreateRosettaStoneTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, _ ->
            if (!command.formattedLabel.value.contains("{$index}")) {
                throw MissingFormattedLabelPlaceholder(index)
            }
        }
        return state
    }
}
