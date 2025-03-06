package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TableThingDefinitionCreateValidator(
    private val thingDefinitionValidator: ThingDefinitionValidator,
) : CreateTableAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(ThingDefinitionValidator(thingRepository, classRepository))

    override fun invoke(command: CreateTableCommand, state: State): State =
        state.copy(
            validatedIds = thingDefinitionValidator.validateThingDefinitions(
                thingDefinitions = command,
                tempIds = state.tempIds,
                validatedIds = state.validatedIds
            )
        )
}
