package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases

class RosettaStoneStatementExistenceValidator(
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases
) : UpdateRosettaStoneStatementAction {
    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State {
        val rosettaStoneStatement = rosettaStoneStatementService.findByIdOrVersionId(command.id)
            .orElseThrow { RosettaStoneStatementNotFound(command.id) }
        return state.copy(rosettaStoneStatement = rosettaStoneStatement)
    }
}
