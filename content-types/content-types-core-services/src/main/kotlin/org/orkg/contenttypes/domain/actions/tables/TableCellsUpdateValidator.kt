package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State.CellGraph
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State.RowGraph
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.Predicates
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
        val directStatements = state.statements[command.tableId].orEmpty()
        val existingRows = directStatements.wherePredicate(Predicates.csvwRows)
            .map { row ->
                val rowStatements = state.statements[row.`object`.id].orEmpty()
                val rowIndexStatement = rowStatements.wherePredicate(Predicates.csvwNumber).singleOrNull()
                val rowIndex = rowIndexStatement?.`object`?.label?.toIntOrNull()
                val labelStatement = rowStatements.wherePredicate(Predicates.csvwTitles).singleOrNull()
                val columnToCell = rowStatements.wherePredicate(Predicates.csvwCells)
                    .map { cell ->
                        val cellStatements = state.statements[cell.`object`.id].orEmpty()
                        val columnStatement = cellStatements.wherePredicate(Predicates.csvwColumn).singleOrNull()
                        val columnIndex = columnStatement?.let { column ->
                            state.statements[column.`object`.id].orEmpty()
                                .wherePredicate(Predicates.csvwNumber).singleOrNull()?.`object`?.label?.toIntOrNull()
                        }
                        val valueStatement = cellStatements.wherePredicate(Predicates.csvwValue).singleOrNull()
                        columnIndex to CellGraph(cell, valueStatement, columnStatement)
                    }
                    .filter { it.first in state.headerIndices }
                    .toMap()
                val cells = state.headerIndices.map { columnIndex -> columnToCell[columnIndex] }
                rowIndex to RowGraph(row, labelStatement, rowIndexStatement, cells)
            }
            .filter { it.first != null }
            .sortedBy { it.first }
            .map { it.second }

        return state.copy(validationCache = validationCache, existingRows = existingRows)
    }
}
