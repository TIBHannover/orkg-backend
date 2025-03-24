package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.ThingsCommandValidator
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TableThingsCommandCreateValidator(
    private val thingsCommandValidator: ThingsCommandValidator,
) : CreateTableAction {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
    ) : this(ThingsCommandValidator(thingRepository, classRepository))

    override fun invoke(command: CreateTableCommand, state: State): State =
        state.copy(
            validationCache = thingsCommandValidator.validate(
                thingsCommand = command,
                validationCache = state.validationCache
            )
        )
}
