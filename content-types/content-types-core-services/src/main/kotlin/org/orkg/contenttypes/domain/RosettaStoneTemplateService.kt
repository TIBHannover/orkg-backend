package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.actions.DescriptionValidator
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateClosedCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateDescriptionCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateDescriptionUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateDescriptionUpdater
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateExampleUsageUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateExampleUsageUpdater
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateExistenceValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateFormattedLabelCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateFormattedLabelCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateFormattedLabelUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateFormattedLabelUpdater
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateLabelUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateModifiableValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplatePropertiesCreateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplatePropertiesCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplatePropertiesUpdateValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateResourceCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateResourceUpdater
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateTargetClassCreator
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.Optional

@Component
class RosettaStoneTemplateService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val resourceService: ResourceUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val classService: ClassUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository,
    private val contributorRepository: ContributorRepository,
    private val thingRepository: ThingRepository,
) : RosettaStoneTemplateUseCases {
    override fun findById(id: ThingId): Optional<RosettaStoneTemplate> =
        resourceRepository.findById(id)
            .filter { Classes.rosettaNodeShape in it.classes }
            .map { it.toRosettaStoneTemplate() }

    override fun findAll(
        searchString: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        pageable: Pageable,
    ): Page<RosettaStoneTemplate> =
        resourceRepository.findAll(
            label = searchString,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            includeClasses = setOf(Classes.rosettaNodeShape),
            organizationId = organizationId,
            observatoryId = observatoryId,
            pageable = pageable
        ).pmap { it.toRosettaStoneTemplate() }

    override fun create(command: CreateRosettaStoneTemplateCommand): ThingId {
        val steps = listOf(
            LabelValidator { it.label },
            DescriptionValidator { it.description },
            RosettaStoneTemplateFormattedLabelCreateValidator(),
            RosettaStoneTemplatePropertiesCreateValidator(predicateRepository, classRepository),
            OrganizationValidator(organizationRepository, { it.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            RosettaStoneTemplateResourceCreator(unsafeResourceUseCases),
            RosettaStoneTemplateTargetClassCreator(classService, unsafeStatementUseCases, unsafeLiteralUseCases),
            RosettaStoneTemplateDescriptionCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            RosettaStoneTemplateFormattedLabelCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            RosettaStoneTemplateClosedCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            RosettaStoneTemplatePropertiesCreator(unsafeResourceUseCases, unsafeLiteralUseCases, unsafeStatementUseCases)
        )
        return steps.execute(command, CreateRosettaStoneTemplateState()).rosettaStoneTemplateId!!
    }

    override fun update(command: UpdateRosettaStoneTemplateCommand) {
        val steps = listOf(
            RosettaStoneTemplateExistenceValidator(this, resourceRepository),
            RosettaStoneTemplateModifiableValidator(rosettaStoneStatementRepository),
            RosettaStoneTemplateLabelUpdateValidator(),
            RosettaStoneTemplateDescriptionUpdateValidator(),
            RosettaStoneTemplateFormattedLabelUpdateValidator(),
            RosettaStoneTemplateExampleUsageUpdateValidator(),
            RosettaStoneTemplatePropertiesUpdateValidator(predicateRepository, classRepository),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.rosettaStoneTemplate!!.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.rosettaStoneTemplate!!.observatories }),
            RosettaStoneTemplateResourceUpdater(unsafeResourceUseCases),
            RosettaStoneTemplateDescriptionUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            RosettaStoneTemplateFormattedLabelUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            RosettaStoneTemplateExampleUsageUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            RosettaStoneTemplatePropertiesUpdater(unsafeLiteralUseCases, resourceService, unsafeResourceUseCases, statementService, unsafeStatementUseCases),
        )
        steps.execute(command, UpdateRosettaStoneTemplateState())
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        resourceService.findById(id).ifPresent { template ->
            if (Classes.rosettaNodeShape !in template.classes) {
                throw RosettaStoneTemplateNotFound(template.id)
            }

            if (!template.modifiable) {
                throw RosettaStoneTemplateNotModifiable(template.id)
            }

            if (thingRepository.isUsedAsObject(template.id)) {
                throw RosettaStoneTemplateInUse.cantBeDeleted(template.id)
            }

            val rosettaStoneStatements = rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )

            if (rosettaStoneStatements.totalElements > 0) {
                throw RosettaStoneTemplateInUse.cantBeDeleted(template.id)
            }

            if (!template.isOwnedBy(contributorId)) {
                val contributor = contributorRepository.findById(contributorId)
                    .orElseThrow { ContributorNotFound(contributorId) }
                if (!contributor.isCurator) {
                    throw NeitherOwnerNorCurator(contributorId)
                }
            }

            val statements = findSubgraph(template).statements
            val directStatements = statements[template.id].orEmpty()
            val propertyStatements = directStatements.wherePredicate(Predicates.shProperty)
                .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
            val statementsToRemove = directStatements.map { it.id } +
                propertyStatements.flatMap { statements[it.`object`.id].orEmpty() }.map { it.id }
            val thingsToRemove = propertyStatements.map { it.`object`.id } + template.id
            statementRepository.deleteByStatementIds(statementsToRemove.toSet())
            thingsToRemove.forEach { resourceService.tryDelete(it, contributorId) }
        }
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph = ContentTypeSubgraph(
        root = resource.id,
        statements = statementRepository.fetchAsBundle(
            id = resource.id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 2,
                blacklist = emptyList(),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        ).groupBy { it.subject.id }
    )

    internal fun Resource.toRosettaStoneTemplate(): RosettaStoneTemplate =
        RosettaStoneTemplate.from(this, findSubgraph(this).statements)
}
