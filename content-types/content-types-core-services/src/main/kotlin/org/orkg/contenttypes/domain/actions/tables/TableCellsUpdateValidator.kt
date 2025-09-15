package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.output.ThingRepository

class TableCellsUpdateValidator(
    private val abstractTableCellsValidator: AbstractTableCellsValidator,
) : UpdateTableAction {
    constructor(thingRepository: ThingRepository) : this(AbstractTableCellsValidator(thingRepository))

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }
        val validationCache = abstractTableCellsValidator.validate(
            rows = command.rows!!,
            thingCommands = command.all(),
            validationCacheIn = state.validationCache
        )
        val existingRows = parseRowGraphs(command.tableId, state.statements, state.headerIndices)
        return state.copy(validationCache = validationCache, existingRows = existingRows)
    }
}
