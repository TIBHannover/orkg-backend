package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TableColumnsCreator(
    private val abstractTableColumnCreator: AbstractTableColumnCreator,
) : CreateTableAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        AbstractTableColumnCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: CreateTableCommand, state: State): State {
        val header = command.rows.first()
        val columns = header.data.mapIndexed { index, value ->
            abstractTableColumnCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = index,
                titleLiteralId = state.resolve(value!!)
            )
        }
        return state.copy(columns = columns)
    }

    private fun State.resolve(id: String) =
        validationCache[id]!!.fold({ tempIdToThing[id] }, { it.id })!!
}
