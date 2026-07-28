package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementThingsCommandUpdateValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : UpdateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
    ) : this(
        ThingsCommandValidator(thingRepository),
    )

    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache,
            ),
        )
}
