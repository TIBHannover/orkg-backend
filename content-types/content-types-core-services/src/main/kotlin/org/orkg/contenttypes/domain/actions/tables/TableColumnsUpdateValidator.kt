package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.output.ThingRepository

class TableColumnsUpdateValidator(
    private val abstractTableColumnsValidator: AbstractTableColumnsValidator,
) : UpdateTableAction {
    constructor(thingRepository: ThingRepository) : this(AbstractTableColumnsValidator(thingRepository))

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }

        val validationCache = abstractTableColumnsValidator.validate(
            rows = command.rows!!,
            thingCommands = command.all(),
            validationCacheIn = state.validationCache
        )
        val headerIndexToStatements = parseColumnGraphs(command.tableId, state.statements)
        val existingColumns = headerIndexToStatements.map { it.second }
        val headerIndices = headerIndexToStatements.map { it.first!! }
        return state.copy(
            validationCache = validationCache,
            headerIndices = headerIndices,
            existingColumns = existingColumns,
        )
    }
}
