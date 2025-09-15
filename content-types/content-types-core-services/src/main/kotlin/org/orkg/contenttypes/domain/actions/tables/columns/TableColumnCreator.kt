package org.orkg.contenttypes.domain.actions.tables.columns

import org.orkg.contenttypes.domain.actions.CreateTableColumnCommand
import org.orkg.contenttypes.domain.actions.TempIdValidator
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
import org.orkg.contenttypes.domain.actions.tables.TableThingsCommandUpdateCreator
import org.orkg.contenttypes.domain.actions.tables.TableThingsCommandUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.TableUpdateValidationCacheInitializer
import org.orkg.contenttypes.domain.actions.tables.columns.CreateTableColumnAction.State
import org.orkg.contenttypes.domain.actions.tables.toRowCommand
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class TableColumnCreator(
    private val thingRepository: ThingRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val classRepository: ClassRepository,
    private val unsafeClassUseCases: UnsafeClassUseCases,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
    private val statementRepository: StatementRepository,
    private val listService: ListUseCases,
) : CreateTableColumnAction {
    override fun invoke(command: CreateTableColumnCommand, state: State): State {
        val columnCount = state.table!!.rows.firstOrNull()?.data?.size ?: 0
        val columnIndex = (command.columnIndex ?: columnCount).coerceAtMost(columnCount)
        val rows = state.table.rows.mapIndexed { index, row ->
            val rowCommand = row.toRowCommand()
            val updatedData = rowCommand.data.toMutableList()
            updatedData.add(columnIndex, command.column[index])
            rowCommand.copy(data = updatedData)
        }
        val updateCommand = UpdateTableCommand(
            tableId = command.tableId,
            contributorId = command.contributorId,
            label = null,
            resources = command.resources,
            literals = command.literals,
            predicates = command.predicates,
            classes = command.classes,
            lists = command.lists,
            rows = rows,
            observatories = null,
            organizations = null,
            extractionMethod = null,
            visibility = null,
        )
        val steps = listOf(
            TempIdValidator { it.tempIds() },
            TableDimensionsValidator { it.rows },
            TableRowsValidator { it.rows },
            TableThingsCommandUpdateValidator(thingRepository, classRepository),
            TableUpdateValidationCacheInitializer(),
            TableColumnsUpdateValidator(thingRepository),
            TableCellsUpdateValidator(thingRepository),
            TableThingsCommandUpdateCreator(unsafeClassUseCases, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, unsafePredicateUseCases, statementRepository, listService),
            TableColumnsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableRowsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableCellsUpdater(unsafeResourceUseCases, unsafeStatementUseCases),
        )
        val updateTableState = UpdateTableState(table = state.table, statements = state.statements)
        val columnId = steps.execute(updateCommand, updateTableState).columns[columnIndex]
        return state.copy(columnId = columnId)
    }
}
