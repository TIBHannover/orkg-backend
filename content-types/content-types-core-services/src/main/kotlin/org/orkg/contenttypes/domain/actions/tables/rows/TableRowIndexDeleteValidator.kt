package org.orkg.contenttypes.domain.actions.tables.rows

import org.orkg.contenttypes.domain.CannotDeleteTableHeader
import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.TooFewTableRows
import org.orkg.contenttypes.domain.actions.DeleteTableRowCommand
import org.orkg.contenttypes.domain.actions.tables.rows.DeleteTableRowAction.State

class TableRowIndexDeleteValidator : DeleteTableRowAction {
    override fun invoke(command: DeleteTableRowCommand, state: State): State {
        val rows = state.table!!.rows
        if (rows.size <= 1) {
            throw TooFewTableRows(command.tableId)
        }
        if (command.rowIndex == 0) {
            throw CannotDeleteTableHeader()
        }
        if (command.rowIndex !in 1 until rows.size) {
            throw TableRowNotFound(command.tableId, command.rowIndex)
        }
        return state
    }
}
