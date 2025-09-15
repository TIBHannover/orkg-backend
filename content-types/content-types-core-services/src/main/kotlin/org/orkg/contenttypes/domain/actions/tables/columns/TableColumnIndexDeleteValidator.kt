package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.contenttypes.domain.TableColumnNotFound
import org.orkg.contenttypes.domain.TooFewTableColumns
import org.orkg.contenttypes.domain.actions.DeleteTableColumnCommand
import org.orkg.contenttypes.domain.actions.tables.columns.DeleteTableColumnAction.State

class TableColumnIndexDeleteValidator : DeleteTableColumnAction {
    override fun invoke(command: DeleteTableColumnCommand, state: State): State {
        val columnCount = state.table!!.rows.firstOrNull()?.data?.size ?: 0
        if (columnCount <= 1) {
            throw TooFewTableColumns(command.tableId)
        }
        if (command.columnIndex !in 1 until columnCount) {
            throw TableColumnNotFound(command.tableId, command.columnIndex)
        }
        return state
    }
}
