package org.orkg.contenttypes.domain.actions.rosettastone.templates

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label

class RosettaStoneTemplateLabelUpdateValidator : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.label != null && command.label != state.rosettaStoneTemplate!!.label) {
            if (state.isUsedInRosettaStoneStatement) {
                throw RosettaStoneTemplateInUse.cantUpdateProperty(command.templateId, "label")
            }
            Label.ofOrNull(command.label!!) ?: throw InvalidLabel()
        }
        return state
    }
}
