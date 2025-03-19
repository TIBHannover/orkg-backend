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
        if (command.contents == null) {
            return state
        }
        val validatedIds = validateThingDefinitions(
            thingDefinitions = command.contents!!,
            tempIds = state.tempIds,
            validatedIds = state.validatedIds
        )
        return state.copy(validatedIds = validatedIds)
    }
}
