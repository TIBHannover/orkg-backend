package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.CreateRowCommand

class TableDimensionsValidator<T, S>(
    private val valueSelector: (T) -> List<CreateRowCommand>?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val rows = valueSelector(command) ?: return state
        val header = rows.firstOrNull() ?: throw MissingTableRows()
        header.data.forEachIndexed { index, label ->
            if (label.isNullOrBlank()) {
                throw MissingTableHeaderValue(index)
            }
        }
        val expectedSize = header.data.size
        rows.forEachIndexed { index, row ->
            if (row.data.size > expectedSize) {
                throw TooManyTableRowValues(index, expectedSize)
            } else if (row.data.size < expectedSize) {
                throw MissingTableRowValues(index, expectedSize)
            }
        }
        return state
    }
}
