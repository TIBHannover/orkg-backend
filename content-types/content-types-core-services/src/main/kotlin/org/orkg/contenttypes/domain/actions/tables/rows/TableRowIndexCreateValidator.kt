package org.orkg.contenttypes.domain.actions.tables.rows

import org.orkg.contenttypes.domain.InvalidTableRowIndex
import org.orkg.contenttypes.domain.actions.CreateTableRowCommand
import org.orkg.contenttypes.domain.actions.tables.rows.CreateTableRowAction.State

class TableRowIndexCreateValidator : CreateTableRowAction {
    override fun invoke(command: CreateTableRowCommand, state: State): State {
        command.rowIndex?.also { rowIndex ->
            if (rowIndex < 0) {
                throw InvalidTableRowIndex(rowIndex)
            }
        }
        return state
    }
}
