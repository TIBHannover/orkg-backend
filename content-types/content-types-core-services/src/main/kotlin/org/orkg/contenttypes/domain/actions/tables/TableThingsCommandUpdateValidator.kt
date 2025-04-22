package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TableThingsCommandUpdateValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : UpdateTableAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(
        ThingsCommandValidator(thingRepository, classRepository)
    )

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }
        val validationCache = thingsCommandValidator.validate(
            thingsCommand = command,
            validationCache = state.validationCache
        )
        return state.copy(validationCache = validationCache)
    }
}
