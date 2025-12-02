package org.orkg.contenttypes.domain.actions.tables.rows

import org.orkg.contenttypes.domain.actions.DeleteTableRowCommand
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
import org.orkg.contenttypes.domain.actions.tables.rows.DeleteTableRowAction.State
import org.orkg.contenttypes.domain.actions.tables.toCreateRowCommand
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ThingRepository

class TableRowDeleter(
    private val thingRepository: ThingRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : DeleteTableRowAction {
    override fun invoke(command: DeleteTableRowCommand, state: State): State {
        val rows = state.table!!.rows.toMutableList()
        rows.removeAt(command.rowIndex)
        val updateCommand = UpdateTableCommand(
            tableId = command.tableId,
            contributorId = command.contributorId,
            label = null,
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            classes = emptyMap(),
            lists = emptyMap(),
            rows = rows.map { it.toCreateRowCommand() },
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
