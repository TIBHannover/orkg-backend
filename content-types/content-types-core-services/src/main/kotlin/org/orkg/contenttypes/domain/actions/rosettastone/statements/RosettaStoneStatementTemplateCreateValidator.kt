package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases

class RosettaStoneStatementTemplateCreateValidator(
    private val rosettaStoneTemplateService: RosettaStoneTemplateUseCases
) : CreateRosettaStoneStatementAction {
    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val rosettaStoneTemplate = rosettaStoneTemplateService.findById(command.templateId)
            .orElseThrow { RosettaStoneTemplateNotFound(command.templateId) }
        return state.copy(rosettaStoneTemplate = rosettaStoneTemplate)
    }
}
