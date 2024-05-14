package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementThingDefinitionCreateValidator(
    private val thingDefinitionValidator: ThingDefinitionValidator
) : CreateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository
    ) : this(ThingDefinitionValidator(thingRepository, classRepository))

    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State =
        state.copy(
            validatedIds = thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        )
}
