package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.contenttypes.domain.actions.DeleteTableColumnCommand
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.tables.TableCellsUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.TableCellsUpdater
import org.orkg.contenttypes.domain.actions.tables.TableColumnsUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.TableColumnsUpdater
import org.orkg.contenttypes.domain.actions.tables.TableDimensionsValidator
import org.orkg.contenttypes.domain.actions.tables.TableRowsUpdater
import org.orkg.contenttypes.domain.actions.tables.TableRowsValidator
import org.orkg.contenttypes.domain.actions.tables.TableUpdateValidationCacheInitializer
import org.orkg.contenttypes.domain.actions.tables.columns.DeleteTableColumnAction.State
import org.orkg.contenttypes.domain.actions.tables.toRowCommand
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ThingRepository

class TableColumnDeleter(
    private val thingRepository: ThingRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : DeleteTableColumnAction {
    override fun invoke(command: DeleteTableColumnCommand, state: State): State {
        val columns = state.table!!.rows.map { row ->
            val updatedData = row.data.toMutableList()
            updatedData.removeAt(command.columnIndex)
            row.copy(data = updatedData)
        }
        val updateCommand = UpdateTableCommand(
            tableId = command.tableId,
            contributorId = command.contributorId,
            label = null,
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            classes = emptyMap(),
            lists = emptyMap(),
            rows = columns.map { it.toRowCommand() },
            observatories = null,
            organizations = null,
            extractionMethod = null,
            visibility = null,
        )
        val steps = listOf(
            TableDimensionsValidator { it.rows },
            TableRowsValidator { it.rows },
            TableUpdateValidationCacheInitializer(),
            TableColumnsUpdateValidator(thingRepository),
            TableCellsUpdateValidator(thingRepository),
            TableColumnsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableRowsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableCellsUpdater(unsafeResourceUseCases, unsafeStatementUseCases),
        )
        val updateTableState = UpdateTableState(table = state.table, statements = state.statements)
        steps.execute(updateCommand, updateTableState)
        return state
    }
}
