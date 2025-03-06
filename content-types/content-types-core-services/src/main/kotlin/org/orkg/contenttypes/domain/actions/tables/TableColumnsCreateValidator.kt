package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.output.ThingRepository

class TableColumnsCreateValidator(
    private val abstractTableColumnsValidator: AbstractTableColumnsValidator,
) : CreateTableAction {
    constructor(thingRepository: ThingRepository) : this(AbstractTableColumnsValidator(thingRepository))

    override fun invoke(command: CreateTableCommand, state: State): State {
        val validatedIds = abstractTableColumnsValidator.validate(
            thingDefinitions = command.all(),
            rows = command.rows,
            tempIds = state.tempIds,
            validationCacheIn = state.validatedIds
        )
        return state.copy(validatedIds = validatedIds)
    }
}
