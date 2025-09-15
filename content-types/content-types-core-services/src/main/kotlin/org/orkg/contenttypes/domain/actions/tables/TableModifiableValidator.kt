package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.actions.Action

class TableModifiableValidator<T, S>(
    private val tableSelector: (S) -> Table,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val table = tableSelector(state)
        if (!table.modifiable) {
            throw TableNotModifiable(table.id)
        }
        return state
    }
}
