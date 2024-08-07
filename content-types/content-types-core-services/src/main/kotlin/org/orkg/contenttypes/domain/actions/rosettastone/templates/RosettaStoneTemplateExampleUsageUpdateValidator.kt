package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State

class RosettaStoneTemplateExampleUsageUpdateValidator : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        val newExampleUsage = command.exampleUsage
        val oldExampleUsage = state.rosettaStoneTemplate!!.exampleUsage
        if (
            newExampleUsage != null && oldExampleUsage != null && newExampleUsage != oldExampleUsage &&
            state.isUsedInRosettaStoneStatement && !newExampleUsage.startsWith(oldExampleUsage)
        ) {
            throw NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage()
        }
        return state
    }
}
