package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TableRowsCreator(
    private val abstractTableRowCreator: AbstractTableRowCreator,
) : CreateTableAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        AbstractTableRowCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: CreateTableCommand, state: State): State {
        val rows = command.rows.asSequence().drop(1).mapIndexed { index, row ->
            abstractTableRowCreator.create(
                contributorId = command.contributorId,
                tableId = state.tableId!!,
                index = index,
                label = row.label
            )
        }
        return state.copy(rows = rows.toList())
    }
}
