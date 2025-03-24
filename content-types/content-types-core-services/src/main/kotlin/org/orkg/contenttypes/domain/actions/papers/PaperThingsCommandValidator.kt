package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class PaperThingsCommandValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : CreatePaperAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(ThingsCommandValidator(thingRepository, classRepository))

    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        if (command.contents == null) {
            return state
        }
        return state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command.contents!!,
                validationCache = state.validationCache
            )
        )
    }
}
