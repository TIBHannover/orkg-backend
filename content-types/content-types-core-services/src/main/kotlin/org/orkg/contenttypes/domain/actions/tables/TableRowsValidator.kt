package org.orkg.contenttypes.domain.actions.tables

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.RowDefinition
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label

class TableRowsValidator<T, S>(
    private val valueSelector: (T) -> List<RowDefinition>,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val rows = valueSelector(command)
        rows.forEachIndexed { index, row ->
            if (row.label != null) {
                Label.ofOrNull(row.label!!) ?: throw InvalidLabel("rows[$index].label")
            }
        }
        return state
    }
}
