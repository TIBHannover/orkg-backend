package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.contenttypes.domain.InvalidTableColumnIndex
import org.orkg.contenttypes.domain.actions.CreateTableColumnCommand
import org.orkg.contenttypes.domain.actions.tables.columns.CreateTableColumnAction.State

class TableColumnIndexCreateValidator : CreateTableColumnAction {
    override fun invoke(command: CreateTableColumnCommand, state: State): State {
        command.columnIndex?.also { columnIndex ->
            if (columnIndex < 0) {
                throw InvalidTableColumnIndex(columnIndex)
            }
        }
        return state
    }
}
