package org.orkg.contenttypes.domain.actions.tables.rows

import org.orkg.contenttypes.domain.TableRowNotFound
import org.orkg.contenttypes.domain.actions.UpdateTableRowCommand
import org.orkg.contenttypes.domain.actions.tables.rows.UpdateTableRowAction.State

class TableRowIndexUpdateValidator : UpdateTableRowAction {
    override fun invoke(command: UpdateTableRowCommand, state: State): State {
        if (command.rowIndex !in 0 until state.table!!.rows.size) {
            throw TableRowNotFound(command.tableId, command.rowIndex)
        }
        return state
    }
}
