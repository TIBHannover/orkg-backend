package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.output.ThingRepository

class TableCellsCreateValidator(
    private val abstractTableCellsValidator: AbstractTableCellsValidator,
) : CreateTableAction {
    constructor(thingRepository: ThingRepository) : this(AbstractTableCellsValidator(thingRepository))

    override fun invoke(command: CreateTableCommand, state: State): State {
        val validatedIds = abstractTableCellsValidator.validate(
            rows = command.rows,
            tempIds = state.tempIds,
            validationCacheIn = state.validatedIds
        )
        return state.copy(validatedIds = validatedIds)
    }
}
