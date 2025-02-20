package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State

class RosettaStoneStatementTempIdCreateValidator(
    private val tempIdValidator: TempIdValidator = TempIdValidator(),
) : CreateRosettaStoneStatementAction {
    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val ids = tempIdValidator.run { command.tempIds() }
        if (ids.isNotEmpty()) {
            tempIdValidator.validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
