package org.orkg.contenttypes.domain.actions.rosettastone.templates

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription

class RosettaStoneTemplateDescriptionUpdateValidator : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.description != null && command.description != state.rosettaStoneTemplate!!.description) {
            if (state.isUsedInRosettaStoneStatement) {
                throw RosettaStoneTemplateInUse.cantUpdateProperty(command.templateId, "description")
            }
            Description.ofOrNull(command.description!!) ?: throw InvalidDescription()
        }
        return state
    }
}
