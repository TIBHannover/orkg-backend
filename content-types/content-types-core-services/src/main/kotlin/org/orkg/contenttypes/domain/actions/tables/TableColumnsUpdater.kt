package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TableColumnsUpdater(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val abstractTableColumnCreator: AbstractTableColumnCreator,
) : UpdateTableAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        unsafeStatementUseCases,
        AbstractTableColumnCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }

        val newHeader = command.rows!!.first().data
        val existingColumns = state.existingColumns
        val thingsToDelete = state.thingsToDelete.toMutableSet()
        val statementsToDelete = state.statementsToDelete.toMutableSet()

        newHeader.zip(existingColumns).forEach { (value, columnGraph) ->
            val existingColumnTitleId = columnGraph.labelStatement?.`object`?.id
            if (value != existingColumnTitleId?.value) {
                if (columnGraph.labelStatement != null) {
                    statementsToDelete += columnGraph.labelStatement.id
                }
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        subjectId = columnGraph.columnId,
                        predicateId = Predicates.csvwTitles,
                        objectId = state.resolve(value!!)!!
                    )
                )
            }
        }

        // create missing columns
        val newColumnIds = newHeader.withIndex()
            .drop(existingColumns.size)
            .map { (index, value) ->
                abstractTableColumnCreator.create(
                    contributorId = command.contributorId,
                    tableId = command.tableId,
                    index = index,
                    titleLiteralId = state.resolve(value!!)!!
                )
            }

        // delete exceeding columns
        existingColumns.drop(newHeader.size).forEach { columnGraph ->
            thingsToDelete += columnGraph.columnId
            statementsToDelete += columnGraph.statementIds
        }

        val columns = existingColumns.take(newHeader.size).map { it.columnId } + newColumnIds

        return state.copy(
            columns = columns,
            thingsToDelete = thingsToDelete,
            statementsToDelete = statementsToDelete
        )
    }
}
