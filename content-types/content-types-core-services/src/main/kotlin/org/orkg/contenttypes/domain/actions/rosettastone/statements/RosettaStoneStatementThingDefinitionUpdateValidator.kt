package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementThingDefinitionUpdateValidator(
    private val thingDefinitionValidator: ThingDefinitionValidator,
) : UpdateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(ThingDefinitionValidator(thingRepository, classRepository))

    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State =
        state.copy(
            validatedIds = thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        )
}
