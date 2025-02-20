package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ThingDefinitionValidator
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class PaperThingDefinitionValidator(
    thingRepository: ThingRepository,
    classRepository: ClassRepository,
) : ThingDefinitionValidator(thingRepository, classRepository),
    CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val validatedIds = state.validatedIds.toMutableMap()
        if (command.contents != null) {
            validateThingDefinitionsInPlace(command.contents!!, state.tempIds, validatedIds)
        }
        return state.copy(validatedIds = validatedIds)
    }
}
