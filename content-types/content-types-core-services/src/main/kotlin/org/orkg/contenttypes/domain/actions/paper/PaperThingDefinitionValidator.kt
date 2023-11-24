package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.graph.output.ThingRepository

class PaperThingDefinitionValidator(
    thingRepository: ThingRepository
) : ThingDefinitionValidator(thingRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val validatedIds = state.validatedIds.toMutableMap()
        if (command.contents != null) {
            validateIdsInDefinitions(command.contents!!, state.tempIds, validatedIds)
        }
        return state.copy(validatedIds = validatedIds)
    }
}
