package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TableCellsCreator(
    private val abstractTableCellCreator: AbstractTableCellCreator,
) : CreateTableAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractTableCellCreator(unsafeResourceUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: CreateTableCommand, state: State): State {
        command.rows.asSequence().drop(1).forEachIndexed { rowIndex, row ->
            row.data.forEachIndexed { columnIndex, value ->
                abstractTableCellCreator.create(
                    contributorId = command.contributorId,
                    rowId = state.rows[rowIndex],
                    columnId = state.columns[columnIndex],
                    value = value?.let { state.resolve(it) },
                )
            }
        }
        return state
    }

    private fun State.resolve(id: String): ThingId =
        validationCache[id]!!.fold({ tempIdToThing[id] }, { it.id })!!
}
