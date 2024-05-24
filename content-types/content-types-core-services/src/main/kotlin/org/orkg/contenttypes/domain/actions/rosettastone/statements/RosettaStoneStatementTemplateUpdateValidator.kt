package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases

class RosettaStoneStatementTemplateUpdateValidator(
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases
) : UpdateRosettaStoneStatementAction {
    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State {
        val rosettaStoneTemplate = rosettaStoneTemplateService.findById(state.rosettaStoneStatement!!.templateId)
            .orElseThrow { RosettaStoneTemplateNotFound(state.rosettaStoneStatement.templateId) }
        return state.copy(rosettaStoneTemplate = rosettaStoneTemplate)
    }
}
