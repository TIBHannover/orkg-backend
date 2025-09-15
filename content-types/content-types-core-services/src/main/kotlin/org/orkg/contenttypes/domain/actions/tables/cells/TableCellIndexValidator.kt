package org.orkg.contenttypes.domain.actions.tables.cells

import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.actions.UpdateTableCellCommand
import org.orkg.contenttypes.domain.actions.tables.cells.UpdateTableCellAction.State

class TableCellIndexValidator : UpdateTableCellAction {
    override fun invoke(command: UpdateTableCellCommand, state: State): State {
        val columnCount = state.table!!.rows.firstOrNull()?.data?.size ?: 0
        if (command.columnIndex !in 0 until columnCount) {
            throw TableColumnNotFound(command.tableId, command.columnIndex)
        } else if (command.rowIndex !in 0 until state.table.rows.size) {
            throw TableRowNotFound(command.tableId, command.rowIndex)
        }
        return state
    }
}
