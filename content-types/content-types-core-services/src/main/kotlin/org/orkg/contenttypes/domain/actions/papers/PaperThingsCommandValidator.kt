package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class PaperThingsCommandValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : CreatePaperAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(
        ThingsCommandValidator(thingRepository, classRepository)
    )

    override fun invoke(command: CreatePaperCommand, state: State): State {
        if (command.contents != null) {
            return state.copy(
                validationCache = thingsCommandValidator.validate(
                    thingsCommand = command.contents!!,
                    validationCache = state.validationCache
                )
            )
        }
        return state
    }
}
