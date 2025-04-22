package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State

class TableModifiableValidator : UpdateTableAction {
    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (!state.table!!.modifiable) {
            throw TableNotModifiable(command.tableId)
        }
        return state
    }
}
