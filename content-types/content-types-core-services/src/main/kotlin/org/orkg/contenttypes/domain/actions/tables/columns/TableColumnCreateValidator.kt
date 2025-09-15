package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.contenttypes.domain.MissingTableColumnValues
import org.orkg.contenttypes.domain.TooManyTableColumnValues
import org.orkg.contenttypes.domain.actions.CreateTableColumnCommand
import org.orkg.contenttypes.domain.actions.tables.columns.CreateTableColumnAction.State

class TableColumnCreateValidator : CreateTableColumnAction {
    override fun invoke(command: CreateTableColumnCommand, state: State): State {
        if (command.column.size < state.table!!.rows.size) {
            throw MissingTableColumnValues(command.columnIndex ?: 0, state.table.rows.size)
        } else if (command.column.size > state.table.rows.size) {
            throw TooManyTableColumnValues(command.columnIndex ?: 0, state.table.rows.size)
        }
        return state
    }
}
