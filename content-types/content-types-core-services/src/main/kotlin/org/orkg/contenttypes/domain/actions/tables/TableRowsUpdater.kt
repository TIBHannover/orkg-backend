package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateLiteralUseCase

class TableRowsUpdater(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val abstractTableRowCreator: AbstractTableRowCreator,
) : UpdateTableAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
        AbstractTableRowCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }

        val newRows = command.rows!!.drop(1) // drop header
        val existingRows = state.existingRows
        val thingsToDelete = state.thingsToDelete.toMutableSet()
        val statementsToDelete = state.statementsToDelete.toMutableSet()

        newRows.zip(existingRows).forEach { (rowCommand, existingRowGraph) ->
            if (rowCommand.label == null && existingRowGraph.label != null) {
                statementsToDelete += existingRowGraph.labelStatement!!.id
            } else if (rowCommand.label != null && existingRowGraph.label == null) {
                val rowLabelLiteralId = unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        label = rowCommand.label!!
                    )
                )
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        subjectId = existingRowGraph.rowId,
                        predicateId = Predicates.csvwTitles,
                        objectId = rowLabelLiteralId
                    )
                )
            } else if (rowCommand.label != existingRowGraph.label) {
                unsafeLiteralUseCases.update(
                    UpdateLiteralUseCase.UpdateCommand(
                        id = existingRowGraph.labelStatement!!.`object`.id,
                        contributorId = command.contributorId,
                        label = rowCommand.label!!,
                    )
                )
            }
        }

        // create missing rows
        val newRowIds = newRows.withIndex()
            .drop(existingRows.size)
            .map { (index, row) ->
                abstractTableRowCreator.create(
                    contributorId = command.contributorId,
                    tableId = command.tableId,
                    index = index,
                    label = row.label
                )
            }

        // delete exceeding rows
        existingRows.drop(newRows.size).forEach { rowGraph ->
            statementsToDelete += rowGraph.statementIds
            thingsToDelete += rowGraph.rowId
        }

        val rows = existingRows.take(newRows.size).map { it.rowId } + newRowIds

        return state.copy(
            rows = rows,
            thingsToDelete = thingsToDelete,
            statementsToDelete = statementsToDelete
        )
    }
}
