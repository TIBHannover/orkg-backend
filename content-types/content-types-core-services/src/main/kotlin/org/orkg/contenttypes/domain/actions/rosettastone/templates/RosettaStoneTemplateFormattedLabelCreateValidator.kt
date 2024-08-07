package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState

class RosettaStoneTemplateFormattedLabelCreateValidator : CreateRosettaStoneTemplateAction {
    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState {
        command.properties.forEachIndexed { index, _ ->
            if (!command.formattedLabel.value.contains("{$index}")) {
                throw MissingFormattedLabelPlaceholder(index)
            }
        }
        return state
    }
}
