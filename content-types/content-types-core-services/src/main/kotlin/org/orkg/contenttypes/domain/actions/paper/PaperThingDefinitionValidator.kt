package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.graph.output.ThingRepository

class PaperThingDefinitionValidator(
    thingRepository: ThingRepository
) : ThingDefinitionValidator(thingRepository), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val validatedIds = state.validatedIds.toMutableMap()
        if (command.contents != null) {
            validateThingDefinitions(command.contents!!, state.tempIds, validatedIds)
        }
        return state.copy(validatedIds = validatedIds)
    }
}
