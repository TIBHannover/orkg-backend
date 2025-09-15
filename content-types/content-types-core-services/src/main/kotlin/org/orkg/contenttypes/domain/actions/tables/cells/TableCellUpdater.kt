package org.orkg.contenttypes.domain.actions.tables.cells

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTableCellCommand
import org.orkg.contenttypes.domain.actions.tables.AbstractTableCellCreator
import org.orkg.contenttypes.domain.actions.tables.cells.UpdateTableCellAction.State
import org.orkg.contenttypes.domain.actions.tables.parseColumnGraphs
import org.orkg.contenttypes.domain.actions.tables.parseRowGraphs
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TableCellUpdater(
    private val abstractTableCellCreator: AbstractTableCellCreator,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) : UpdateTableCellAction {
    constructor(
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
    ) : this(
        AbstractTableCellCreator(unsafeResourceUseCases, unsafeStatementUseCases),
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
    )

    override fun invoke(command: UpdateTableCellCommand, state: State): State {
        val columnGraphs = parseColumnGraphs(command.tableId, state.statements)
        val columns = columnGraphs.map { it.second }
        if (command.rowIndex == 0) {
            val columnGraph = columns[command.columnIndex]
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = state.statements[columnGraph.columnId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = columnGraph.columnId,
                predicateId = Predicates.csvwTitles,
                objectId = command.id!!,
            )
        } else {
            val headerIndices = columnGraphs.map { it.first!! }
            val rowGraphs = parseRowGraphs(command.tableId, state.statements, headerIndices)
            val rowGraph = rowGraphs[command.rowIndex - 1] // account for header
            val cellGraph = rowGraph.cells[command.columnIndex]
            if (cellGraph == null) {
                abstractTableCellCreator.create(
                    contributorId = command.contributorId,
                    rowId = rowGraph.rowId,
                    columnId = columns[command.columnIndex].columnId,
                    value = command.id,
                )
            } else {
                singleStatementPropertyUpdater.updateOptionalProperty(
                    statements = state.statements[cellGraph.cellId].orEmpty(),
                    contributorId = command.contributorId,
                    subjectId = cellGraph.cellId,
                    predicateId = Predicates.csvwValue,
                    objectId = command.id,
                )
            }
        }
        return state
    }
}
