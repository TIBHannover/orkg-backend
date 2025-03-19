package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class PaperThingsCommandValidator(
    thingRepository: ThingRepository,
    classRepository: ClassRepository,
) : ThingsCommandValidator(thingRepository, classRepository),
    CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        if (command.contents == null) {
            return state
        }
        val validatedIds = validate(
            thingsCommand = command.contents!!,
            tempIds = state.tempIds,
            validatedIds = state.validatedIds
        )
        return state.copy(validatedIds = validatedIds)
    }
}
