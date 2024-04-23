package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
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
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
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
    private val literalService: LiteralUseCases
) : RosettaStoneTemplateUseCases {
    override fun findById(id: ThingId): Optional<RosettaStoneTemplate> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.rosettaNodeShape in it.classes }
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
            LabelValidator("description") { it.description },
            RosettaStoneTemplateFormattedLabelValidator(),
            RosettaStoneTemplatePropertiesValidator(predicateRepository, classRepository),
            OrganizationValidator(organizationRepository, { it.organizations }),
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            RosettaStoneTemplateResourceCreator(resourceService),
            RosettaStoneTemplateTargetClassCreator(classService, statementService),
            RosettaStoneTemplateDescriptionCreator(literalService, statementService),
            RosettaStoneTemplateFormattedLabelCreator(literalService, statementService),
            RosettaStoneTemplateClosedCreator(literalService, statementService),
            RosettaStoneTemplatePropertiesCreator(resourceService, literalService, statementService)
        )
        return steps.execute(command, CreateRosettaStoneTemplateState()).rosettaStoneTemplateId!!
    }

    private fun Resource.toRosettaStoneTemplate(): RosettaStoneTemplate {
        val statements = statementRepository.fetchAsBundle(
            id = id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 2,
                blacklist = emptyList(),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        ).groupBy { it.subject.id }
        return RosettaStoneTemplate(
            id = id,
            label = label,
            description = statements[id]!!
                .wherePredicate(Predicates.description)
                .singleOrNull()?.`object`?.label,
            formattedLabel = statements[id]!!
                .wherePredicate(Predicates.templateLabelFormat)
                .singleOrNull()
                ?.let { FormattedLabel.of(it.`object`.label) },
            targetClass = statements[id]!!
                .wherePredicate(Predicates.shTargetClass)
                .single()
                .`object`.id,
            properties = statements[id]!!
                .wherePredicate(Predicates.shProperty)
                .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
                .mapNotNull { TemplateProperty.from(it.`object` as Resource, statements[it.`object`.id].orEmpty()) }
                .sortedBy { it.order },
            createdAt = createdAt,
            createdBy = createdBy,
            organizations = listOf(organizationId),
            observatories = listOf(observatoryId),
            visibility = visibility,
            unlistedBy = unlistedBy
        )
    }
}
