package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository

class RosettaStoneStatementContextValidator(
    private val resourceRepository: ResourceRepository,
) : CreateRosettaStoneStatementAction {
    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        command.context?.let { context ->
            resourceRepository.findById(context)
                .orElseThrow { ResourceNotFound.withId(context) }
        }
        return state
    }
}
