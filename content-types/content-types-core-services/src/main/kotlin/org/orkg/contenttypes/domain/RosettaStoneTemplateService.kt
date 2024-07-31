package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.actions.DescriptionValidator
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateClosedCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateDescriptionCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateFormattedLabelCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateFormattedLabelValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplatePropertiesCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplatePropertiesValidator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateResourceCreator
import org.orkg.contenttypes.domain.actions.rosettastone.templates.RosettaStoneTemplateTargetClassCreator
import org.orkg.contenttypes.domain.actions.tryDelete
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ContributorNotFound
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class RosettaStoneTemplateService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val resourceService: ResourceUseCases,
    private val classService: ClassUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository,
    private val contributorRepository: ContributorRepository,
    private val thingRepository: ThingRepository
) : RosettaStoneTemplateUseCases {
    override fun findById(id: ThingId): Optional<RosettaStoneTemplate> =
        resourceRepository.findById(id)
            .filter { Classes.rosettaNodeShape in it.classes }
            .map { it.toRosettaStoneTemplate() }

    override fun findAll(
        searchString: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        pageable: Pageable
    ): Page<RosettaStoneTemplate> =
        resourceRepository.findAll(
            label = searchString,
            visibility = visibility,
            createdBy = createdBy,
            includeClasses = setOf(Classes.rosettaNodeShape),
            pageable = pageable
        ).pmap { it.toRosettaStoneTemplate() }

    override fun create(command: CreateRosettaStoneTemplateCommand): ThingId {
        val steps = listOf(
            LabelValidator { it.label },
            DescriptionValidator { it.description },
            RosettaStoneTemplateFormattedLabelValidator(),
            RosettaStoneTemplatePropertiesValidator(predicateRepository, classRepository),
            OrganizationValidator(organizationRepository, { it.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            RosettaStoneTemplateResourceCreator(resourceService),
            RosettaStoneTemplateTargetClassCreator(classService, statementService, literalService),
            RosettaStoneTemplateDescriptionCreator(literalService, statementService),
            RosettaStoneTemplateFormattedLabelCreator(literalService, statementService),
            RosettaStoneTemplateClosedCreator(literalService, statementService),
            RosettaStoneTemplatePropertiesCreator(resourceService, literalService, statementService)
        )
        return steps.execute(command, CreateRosettaStoneTemplateState()).rosettaStoneTemplateId!!
    }

    override fun delete(id: ThingId, contributorId: ContributorId) {
        resourceService.findById(id).ifPresent { template ->
            if (Classes.rosettaNodeShape !in template.classes) {
                throw RosettaStoneTemplateNotFound(template.id)
            }

            if (!template.modifiable) {
                throw RosettaStoneTemplateNotModifiable(template.id)
            }

            if (thingRepository.isUsedAsObject(template.id))
                throw RosettaStoneTemplateInUse(template.id)

            val rosettaStoneStatements = rosettaStoneStatementRepository.findAll(
                templateId = template.id,
                pageable = PageRequests.SINGLE
            )

            if (rosettaStoneStatements.totalElements > 0) {
                throw RosettaStoneTemplateInUse(template.id)
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

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        return ContentTypeSubgraph(
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
    }

    internal fun Resource.toRosettaStoneTemplate(): RosettaStoneTemplate =
        RosettaStoneTemplate.from(this, findSubgraph(this).statements)
}
