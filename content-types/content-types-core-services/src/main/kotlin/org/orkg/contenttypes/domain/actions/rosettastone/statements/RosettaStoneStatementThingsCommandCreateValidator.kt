package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementThingsCommandCreateValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : CreateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(ThingsCommandValidator(thingRepository, classRepository))

    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache
            )
        )
}
