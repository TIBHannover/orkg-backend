package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableColumnCommand
import org.orkg.contenttypes.domain.actions.CreateTableColumnState
import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.CreateTableRowCommand
import org.orkg.contenttypes.domain.actions.CreateTableRowState
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.domain.actions.DeleteTableColumnCommand
import org.orkg.contenttypes.domain.actions.DeleteTableColumnState
import org.orkg.contenttypes.domain.actions.DeleteTableRowCommand
import org.orkg.contenttypes.domain.actions.DeleteTableRowState
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.UpdateTableCellCommand
import org.orkg.contenttypes.domain.actions.UpdateTableCellState
import org.orkg.contenttypes.domain.actions.UpdateTableColumnCommand
import org.orkg.contenttypes.domain.actions.UpdateTableColumnState
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.UpdateTableRowCommand
import org.orkg.contenttypes.domain.actions.UpdateTableRowState
import org.orkg.contenttypes.domain.actions.UpdateTableState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.tables.TableCellsCreateValidator
import org.orkg.contenttypes.domain.actions.tables.TableCellsCreator
import org.orkg.contenttypes.domain.actions.tables.TableCellsUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.TableCellsUpdater
import org.orkg.contenttypes.domain.actions.tables.TableColumnsCreateValidator
import org.orkg.contenttypes.domain.actions.tables.TableColumnsCreator
import org.orkg.contenttypes.domain.actions.tables.TableColumnsUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.TableColumnsUpdater
import org.orkg.contenttypes.domain.actions.tables.TableDimensionsValidator
import org.orkg.contenttypes.domain.actions.tables.TableExistenceValidator
import org.orkg.contenttypes.domain.actions.tables.TableModifiableValidator
import org.orkg.contenttypes.domain.actions.tables.TableResourceCreator
import org.orkg.contenttypes.domain.actions.tables.TableResourceUpdater
import org.orkg.contenttypes.domain.actions.tables.TableRowsCreator
import org.orkg.contenttypes.domain.actions.tables.TableRowsUpdater
import org.orkg.contenttypes.domain.actions.tables.TableRowsValidator
import org.orkg.contenttypes.domain.actions.tables.TableThingsCommandCreateCreator
import org.orkg.contenttypes.domain.actions.tables.TableThingsCommandCreateValidator
import org.orkg.contenttypes.domain.actions.tables.TableThingsCommandUpdateCreator
import org.orkg.contenttypes.domain.actions.tables.TableThingsCommandUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.TableUpdateValidationCacheInitializer
import org.orkg.contenttypes.domain.actions.tables.cells.TableCellIndexValidator
import org.orkg.contenttypes.domain.actions.tables.cells.TableCellUpdater
import org.orkg.contenttypes.domain.actions.tables.cells.TableCellValueValidator
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnCreateValidator
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnCreator
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnDeleter
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnIndexCreateValidator
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnIndexDeleteValidator
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnIndexUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.columns.TableColumnUpdater
import org.orkg.contenttypes.domain.actions.tables.rows.TableRowCreator
import org.orkg.contenttypes.domain.actions.tables.rows.TableRowDeleter
import org.orkg.contenttypes.domain.actions.tables.rows.TableRowIndexCreateValidator
import org.orkg.contenttypes.domain.actions.tables.rows.TableRowIndexDeleteValidator
import org.orkg.contenttypes.domain.actions.tables.rows.TableRowIndexUpdateValidator
import org.orkg.contenttypes.domain.actions.tables.rows.TableRowUpdater
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
class TableService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val thingRepository: ThingRepository,
    private val classRepository: ClassRepository,
    private val unsafeClassUseCases: UnsafeClassUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
    private val listService: ListUseCases,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val statementUseCases: StatementUseCases,
) : TableUseCases {
    override fun findById(id: ThingId): Optional<Table> =
        resourceRepository.findById(id)
            .filter { Classes.table in it.classes }
            .map { it.toTable() }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
    ): Page<Table> =
        resourceRepository.findAll(
            pageable = pageable,
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            includeClasses = setOf(Classes.table)
        ).map { it.toTable() }

    override fun create(command: CreateTableCommand): ThingId {
        val steps = listOf(
            LabelValidator { it.label },
            TempIdValidator { it.tempIds() },
            TableDimensionsValidator { it.rows },
            TableRowsValidator { it.rows },
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            TableThingsCommandCreateValidator(thingRepository, classRepository),
            TableColumnsCreateValidator(thingRepository),
            TableCellsCreateValidator(thingRepository),
            TableResourceCreator(unsafeResourceUseCases),
            TableThingsCommandCreateCreator(unsafeClassUseCases, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, unsafePredicateUseCases, statementRepository, listService),
            TableColumnsCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableRowsCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableCellsCreator(unsafeResourceUseCases, unsafeStatementUseCases)
        )
        return steps.execute(command, CreateTableState()).tableId!!
    }

    override fun update(command: UpdateTableCommand) {
        val steps = listOf<Action<UpdateTableCommand, UpdateTableState>>(
            TableExistenceValidator(this, resourceRepository, UpdateTableCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            LabelValidator { it.label },
            TempIdValidator { it.tempIds() },
            TableDimensionsValidator { it.rows },
            TableRowsValidator { it.rows },
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            TableThingsCommandUpdateValidator(thingRepository, classRepository),
            TableUpdateValidationCacheInitializer(),
            TableColumnsUpdateValidator(thingRepository),
            TableCellsUpdateValidator(thingRepository),
            TableResourceUpdater(unsafeResourceUseCases),
            TableThingsCommandUpdateCreator(unsafeClassUseCases, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, unsafePredicateUseCases, statementRepository, listService),
            TableColumnsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableRowsUpdater(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
            TableCellsUpdater(unsafeResourceUseCases, unsafeStatementUseCases),
        )
        steps.execute(command, UpdateTableState())
    }

    override fun createTableRow(command: CreateTableRowCommand): ThingId {
        val steps = listOf(
            TableExistenceValidator(this, resourceRepository, CreateTableRowCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableRowIndexCreateValidator(),
            TableRowCreator(thingRepository, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, classRepository, unsafeClassUseCases, unsafePredicateUseCases, statementRepository, listService),
        )
        return steps.execute(command, CreateTableRowState()).rowId!!
    }

    override fun updateTableRow(command: UpdateTableRowCommand) {
        val steps = listOf(
            TableExistenceValidator(this, resourceRepository, UpdateTableRowCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableRowIndexUpdateValidator(),
            TableRowUpdater(thingRepository, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, classRepository, unsafeClassUseCases, unsafePredicateUseCases, statementRepository, listService),
        )
        steps.execute(command, UpdateTableRowState())
    }

    override fun deleteTableRow(command: DeleteTableRowCommand) {
        val steps = listOf(
            TableExistenceValidator(this, resourceRepository, DeleteTableRowCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableRowIndexDeleteValidator(),
            TableRowDeleter(thingRepository, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
        )
        steps.execute(command, DeleteTableRowState())
    }

    override fun createTableColumn(command: CreateTableColumnCommand): ThingId {
        val steps = listOf(
            TableExistenceValidator(this, resourceRepository, CreateTableColumnCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableColumnIndexCreateValidator(),
            TableColumnCreateValidator(),
            TableColumnCreator(thingRepository, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, classRepository, unsafeClassUseCases, unsafePredicateUseCases, statementRepository, listService),
        )
        return steps.execute(command, CreateTableColumnState()).columnId!!
    }

    override fun updateTableColumn(command: UpdateTableColumnCommand) {
        val steps = listOf(
            TableExistenceValidator(this, resourceRepository, UpdateTableColumnCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableColumnIndexUpdateValidator(),
            TableColumnUpdater(thingRepository, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, classRepository, unsafeClassUseCases, unsafePredicateUseCases, statementRepository, listService),
        )
        steps.execute(command, UpdateTableColumnState())
    }

    override fun deleteTableColumn(command: DeleteTableColumnCommand) {
        val steps = listOf(
            TableExistenceValidator(this, resourceRepository, DeleteTableColumnCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableColumnIndexDeleteValidator(),
            TableColumnDeleter(thingRepository, unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases),
        )
        steps.execute(command, DeleteTableColumnState())
    }

    override fun updateTableCell(command: UpdateTableCellCommand) {
        val steps = listOf<Action<UpdateTableCellCommand, UpdateTableCellState>>(
            TableExistenceValidator(this, resourceRepository, UpdateTableCellCommand::tableId) { table, statements -> copy(table = table, statements = statements) },
            TableModifiableValidator { it.table!! },
            TableCellIndexValidator(),
            TableCellValueValidator(thingRepository),
            TableCellUpdater(unsafeStatementUseCases, unsafeResourceUseCases, unsafeLiteralUseCases, statementUseCases),
        )
        steps.execute(command, UpdateTableCellState())
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        val statements = statementRepository.fetchAsBundle(
            id = resource.id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 3,
                blacklist = emptyList(),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        )
        return ContentTypeSubgraph(
            root = resource.id,
            statements = statements.groupBy { it.subject.id }
        )
    }

    internal fun Resource.toTable(): Table = Table.from(this, findSubgraph(this).statements)
}
