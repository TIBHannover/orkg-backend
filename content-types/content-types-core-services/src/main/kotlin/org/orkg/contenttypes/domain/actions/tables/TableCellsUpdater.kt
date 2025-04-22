package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TableCellsUpdater(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val abstractTableCellCreator: AbstractTableCellCreator,
) : UpdateTableAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        AbstractTableCellCreator(unsafeResourceUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }

        val newRows = command.rows!!.drop(1) // drop header
        val existingRows = state.existingRows
        val thingsToDelete = state.thingsToDelete.toMutableSet()
        val statementsToDelete = state.statementsToDelete.toMutableSet()

        newRows.zip(existingRows).forEachIndexed { rowIndex, (row, rowGraph) ->
            if (row.matchesRowData(rowGraph.toRow()) == true) {
                // contents are equal, nothing to do
                return@forEachIndexed
            }
            row.data.zip(rowGraph.cells).forEachIndexed { columnIndex, (value, cellGraph) ->
                if (cellGraph == null) {
                    // old cell does not exist, create new one
                    abstractTableCellCreator.create(
                        contributorId = command.contributorId,
                        rowId = state.rows[rowIndex],
                        columnId = state.columns[columnIndex],
                        value = value?.let(state::resolve),
                    )
                } else if (value != cellGraph.value?.id?.value) {
                    // new value differs from old value, unlink old value if present
                    if (cellGraph.valueStatement != null) {
                        statementsToDelete += cellGraph.valueStatement.id
                    }
                    // set new value if present
                    if (value != null) {
                        unsafeStatementUseCases.create(
                            CreateStatementUseCase.CreateCommand(
                                contributorId = command.contributorId,
                                subjectId = cellGraph.cellId,
                                predicateId = Predicates.csvwValue,
                                objectId = state.resolve(value)!!
                            )
                        )
                    }
                }
            }
            // create new cells if there are not enough
            row.data.withIndex().drop(rowGraph.cells.size).forEach { (columnIndex, value) ->
                abstractTableCellCreator.create(
                    contributorId = command.contributorId,
                    rowId = state.rows[rowIndex],
                    columnId = state.columns[columnIndex],
                    value = value?.let(state::resolve),
                )
            }
            // mark exceeding cells for deletion
            rowGraph.cells.drop(row.data.size).forEach { cellGraph ->
                if (cellGraph != null) {
                    statementsToDelete += cellGraph.statementIds
                    thingsToDelete += cellGraph.cellId
                }
            }
        }

        // create cells for missing rows
        newRows.withIndex().drop(existingRows.size).forEach { (rowIndex, row) ->
            row.data.forEachIndexed { columnIndex, value ->
                abstractTableCellCreator.create(
                    contributorId = command.contributorId,
                    rowId = state.rows[rowIndex],
                    columnId = state.columns[columnIndex],
                    value = value?.let(state::resolve),
                )
            }
        }

        // delete cells for exceeding rows
        existingRows.drop(newRows.size).forEach { rowGraph ->
            statementsToDelete += rowGraph.cells.mapNotNull { it?.statementIds }.flatten()
            thingsToDelete += rowGraph.cells.mapNotNull { it?.cellId }
        }

        unsafeStatementUseCases.deleteAllById(statementsToDelete)
        thingsToDelete.forEach { id ->
            // Here, we are assuming that we only need to delete resources.
            // We do that, because there currently exists no deletion method for the ThingRepository.
            // However, the assumption should always be true, as we only delete parts of the table structure,
            // which solely consists of resources and statements, and we do not delete any actual table cell values.

            // All things of the table structure should be in the validation cache at this point.
            // See TableUpdateValidationCacheInitializer.
            val cachedThing = state.validationCache[id.value]
                ?: throw IllegalStateException("""Expected thing "$id" to be cached.""")
            cachedThing.onLeft { throw IllegalStateException("""Expected thing "$id" to exist.""") }
            cachedThing.onRight { thing -> require(thing is Resource) { """Expected thing "${thing.id}" to be a resource.""" } }
            unsafeResourceUseCases.tryDelete(id, command.contributorId)
        }

        return state.copy(thingsToDelete = thingsToDelete, statementsToDelete = statementsToDelete)
    }
}
