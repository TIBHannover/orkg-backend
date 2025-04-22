package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State.ColumnGraph
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.Predicates
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
        val headerIndexToStatements = state.statements[command.tableId].orEmpty()
            .wherePredicate(Predicates.csvwColumns)
            .map { column ->
                val columnStatements = state.statements[column.`object`.id].orEmpty()
                val columnIndexStatement = columnStatements.wherePredicate(Predicates.csvwNumber).singleOrNull()
                val columnIndex = columnIndexStatement?.`object`?.label?.toIntOrNull()
                val labelStatement = columnStatements.wherePredicate(Predicates.csvwTitles).singleOrNull()
                columnIndex to ColumnGraph(column, labelStatement, columnIndexStatement)
            }
            .filter { it.first != null }
            .sortedBy { it.first }
        val existingColumns = headerIndexToStatements.map { it.second }
        val headerIndices = headerIndexToStatements.map { it.first!! }
        return state.copy(
            validationCache = validationCache,
            headerIndices = headerIndices,
            existingColumns = existingColumns,
        )
    }
}
