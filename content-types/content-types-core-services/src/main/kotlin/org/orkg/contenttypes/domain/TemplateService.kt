package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.pmap
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyState
import org.orkg.contenttypes.domain.actions.CreateTemplateState
import org.orkg.contenttypes.domain.actions.DescriptionValidator
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyState
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.actions.VisibilityValidator
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.templates.TemplateClosedCreator
import org.orkg.contenttypes.domain.actions.templates.TemplateClosedUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplateDescriptionCreator
import org.orkg.contenttypes.domain.actions.templates.TemplateDescriptionUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplateExistenceValidator
import org.orkg.contenttypes.domain.actions.templates.TemplateFormattedLabelCreator
import org.orkg.contenttypes.domain.actions.templates.TemplateFormattedLabelUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplatePropertiesCreator
import org.orkg.contenttypes.domain.actions.templates.TemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplatePropertiesValidator
import org.orkg.contenttypes.domain.actions.templates.TemplateRelationsCreateValidator
import org.orkg.contenttypes.domain.actions.templates.TemplateRelationsCreator
import org.orkg.contenttypes.domain.actions.templates.TemplateRelationsUpdateValidator
import org.orkg.contenttypes.domain.actions.templates.TemplateRelationsUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplateResourceCreator
import org.orkg.contenttypes.domain.actions.templates.TemplateResourceUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplateTargetClassCreator
import org.orkg.contenttypes.domain.actions.templates.TemplateTargetClassUpdater
import org.orkg.contenttypes.domain.actions.templates.TemplateTargetClassValidator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyExistenceCreateValidator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyExistenceUpdateValidator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyTemplateCreateValidator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyTemplateUpdateValidator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyUpdater
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyValidator
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.Optional

@Component
class TemplateService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceService: ResourceUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val templateRepository: TemplateRepository,
    private val contributorRepository: ContributorRepository,
) : TemplateUseCases {
    override fun findById(id: ThingId): Optional<Template> =
        statementRepository.findAll(
            subjectId = id,
            subjectClasses = setOf(Classes.resource, Classes.nodeShape),
            predicateId = Predicates.shTargetClass,
            objectClasses = setOf(Classes.`class`),
            pageable = PageRequests.SINGLE
        ).content
            .singleOrNull()
            .let { Optional.ofNullable(it) }
            .map { (it.subject as Resource).toTemplate() }

    override fun findAll(
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        researchProblem: ThingId?,
        targetClass: ThingId?,
        pageable: Pageable,
    ): Page<Template> =
        templateRepository.findAll(
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            researchProblem = researchProblem,
            targetClassId = targetClass,
            pageable = pageable
        ).pmap { it.toTemplate() }

    override fun create(command: CreateTemplateCommand): ThingId {
        val steps = listOf(
            LabelValidator { it.label },
            DescriptionValidator { it.description },
            LabelValidator("formatted_label") { it.formattedLabel?.value },
            TemplateTargetClassValidator(classRepository, statementRepository, { it.targetClass }),
            TemplateRelationsCreateValidator(resourceRepository, predicateRepository),
            TemplatePropertiesValidator(predicateRepository, classRepository, { it.properties }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            TemplateResourceCreator(unsafeResourceUseCases),
            TemplateTargetClassCreator(unsafeStatementUseCases),
            TemplateRelationsCreator(unsafeStatementUseCases),
            TemplateDescriptionCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            TemplateFormattedLabelCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            TemplateClosedCreator(unsafeLiteralUseCases, unsafeStatementUseCases),
            TemplatePropertiesCreator(unsafeResourceUseCases, unsafeLiteralUseCases, unsafeStatementUseCases)
        )
        return steps.execute(command, CreateTemplateState()).templateId!!
    }

    override fun create(command: CreateTemplatePropertyCommand): ThingId {
        val steps = listOf(
            TemplatePropertyExistenceCreateValidator(this, resourceRepository),
            TemplatePropertyTemplateCreateValidator(),
            TemplatePropertyValidator(predicateRepository, classRepository, { it.template!!.properties }, { it }),
            TemplatePropertyCreator(unsafeResourceUseCases, unsafeLiteralUseCases, unsafeStatementUseCases)
        )
        return steps.execute(command, CreateTemplatePropertyState()).templatePropertyId!!
    }

    override fun update(command: UpdateTemplateCommand) {
        val steps = listOf(
            TemplateExistenceValidator(this, resourceRepository),
            LabelValidator { it.label },
            DescriptionValidator { it.description },
            LabelValidator("formatted_label") { it.formattedLabel?.value },
            VisibilityValidator(contributorRepository, { it.contributorId }, { it.template!! }, { it.visibility }),
            TemplateTargetClassValidator(classRepository, statementRepository, { it.targetClass }, { it.template!!.targetClass.id }),
            TemplateRelationsUpdateValidator(resourceRepository, predicateRepository),
            TemplatePropertiesValidator(predicateRepository, classRepository, { it.properties }, { it.template!!.properties }),
            OrganizationValidator(organizationRepository, { it.organizations }, { it.template!!.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }, { it.template!!.observatories }),
            TemplateResourceUpdater(unsafeResourceUseCases),
            TemplateTargetClassUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            TemplateRelationsUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            TemplateDescriptionUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            TemplateFormattedLabelUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            TemplateClosedUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
            TemplatePropertiesUpdater(unsafeLiteralUseCases, resourceService, unsafeResourceUseCases, statementService, unsafeStatementUseCases)
        )
        steps.execute(command, UpdateTemplateState())
    }

    override fun update(command: UpdateTemplatePropertyCommand) {
        val steps = listOf(
            TemplatePropertyExistenceUpdateValidator(this, resourceRepository),
            TemplatePropertyTemplateUpdateValidator(),
            TemplatePropertyValidator(predicateRepository, classRepository, { it.template!!.properties }, { it }, { it.templateProperty }),
            TemplatePropertyUpdater(unsafeLiteralUseCases, unsafeResourceUseCases, statementService, unsafeStatementUseCases)
        )
        steps.execute(command, UpdateTemplatePropertyState())
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

    internal fun Resource.toTemplate(): Template =
        Template.from(this, findSubgraph(this).statements)
}
