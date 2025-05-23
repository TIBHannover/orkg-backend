package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.CreateTableState
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
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
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
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
            TableExistenceValidator(this, resourceRepository),
            TableModifiableValidator(),
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
