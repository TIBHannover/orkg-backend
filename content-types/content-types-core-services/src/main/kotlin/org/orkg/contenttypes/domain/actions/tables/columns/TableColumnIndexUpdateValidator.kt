package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.actions.UpdateTableColumnCommand
import org.orkg.contenttypes.domain.actions.tables.columns.UpdateTableColumnAction.State

class TableColumnIndexUpdateValidator : UpdateTableColumnAction {
    override fun invoke(command: UpdateTableColumnCommand, state: State): State {
        val columnCount = state.table!!.rows.firstOrNull()?.data?.size ?: 0
        if (command.columnIndex !in 0 until columnCount) {
            throw TableColumnNotFound(command.tableId, command.columnIndex)
        }
        return state
    }
}
