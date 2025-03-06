package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State

class TableTempIdCreateValidator(
    private val tempIdValidator: TempIdValidator = TempIdValidator(),
) : CreateTableAction {
    override fun invoke(command: CreateTableCommand, state: State): State {
        val ids = tempIdValidator.run { command.tempIds() }
        if (ids.isNotEmpty()) {
            tempIdValidator.validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
