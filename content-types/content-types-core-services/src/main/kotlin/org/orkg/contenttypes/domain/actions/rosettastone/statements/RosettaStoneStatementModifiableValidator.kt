package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.RosettaStoneStatementNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State

class RosettaStoneStatementModifiableValidator : UpdateRosettaStoneStatementAction {
    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State {
        if (!state.rosettaStoneStatement!!.modifiable) {
            throw RosettaStoneStatementNotModifiable(command.id)
        }
        return state
    }
}
