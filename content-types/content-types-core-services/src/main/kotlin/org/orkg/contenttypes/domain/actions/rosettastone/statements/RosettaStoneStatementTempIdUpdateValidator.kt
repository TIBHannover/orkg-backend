package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State

class RosettaStoneStatementTempIdUpdateValidator(
    private val tempIdValidator: TempIdValidator = TempIdValidator(),
) : UpdateRosettaStoneStatementAction {
    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State {
        val ids = tempIdValidator.run { command.tempIds() }
        if (ids.isNotEmpty()) {
            tempIdValidator.validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
