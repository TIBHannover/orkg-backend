package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.TemplatePropertyState
import org.orkg.contenttypes.domain.actions.TemplateState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.template.TemplateClosedCreator
import org.orkg.contenttypes.domain.actions.template.TemplateDescriptionCreator
import org.orkg.contenttypes.domain.actions.template.TemplateFormattedLabelCreator
import org.orkg.contenttypes.domain.actions.template.TemplatePropertiesCreator
import org.orkg.contenttypes.domain.actions.template.TemplatePropertiesValidator
import org.orkg.contenttypes.domain.actions.template.TemplateRelationsCreator
import org.orkg.contenttypes.domain.actions.template.TemplateRelationsValidator
import org.orkg.contenttypes.domain.actions.template.TemplateResourceCreator
import org.orkg.contenttypes.domain.actions.template.TemplateTargetClassCreator
import org.orkg.contenttypes.domain.actions.template.TemplateTargetClassValidator
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyExistenceValidator
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyTemplateValidator
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyValueCreator
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyValueValidator
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
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
class TemplateService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val templateRepository: TemplateRepository
) : TemplateUseCases {
    override fun findById(id: ThingId): Optional<Template> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.nodeShape in it.classes }
            .map { it.toTemplate() }

    override fun findAll(
        searchString: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        researchField: ThingId?,
        researchProblem: ThingId?,
        targetClass: ThingId?,
        pageable: Pageable
    ): Page<Template> =
        templateRepository.findAll(searchString, visibility, createdBy, researchField, researchProblem, targetClass, pageable)
            .pmap { it.toTemplate() }

    override fun create(command: CreateTemplateCommand): ThingId {
        val steps = listOf(
            TemplateTargetClassValidator(classRepository, statementRepository),
            TemplateRelationsValidator(resourceRepository, predicateRepository),
            TemplatePropertiesValidator(predicateRepository, classRepository),
            OrganizationValidator(organizationRepository) { it.organizations },
            ObservatoryValidator(observatoryRepository) { it.observatories },
            TemplateResourceCreator(resourceService),
            TemplateTargetClassCreator(statementService),
            TemplateRelationsCreator(statementService),
            TemplateDescriptionCreator(literalService, statementService),
            TemplateFormattedLabelCreator(literalService, statementService),
            TemplateClosedCreator(literalService, statementService),
            TemplatePropertiesCreator(resourceService, literalService, statementService)
        )
        return steps.execute(command, TemplateState()).templateId!!
    }

    override fun createTemplateProperty(command: CreateTemplatePropertyCommand): ThingId {
        val steps = listOf(
            TemplatePropertyExistenceValidator(resourceRepository),
            TemplatePropertyTemplateValidator(statementRepository),
            TemplatePropertyValueValidator(predicateRepository, classRepository),
            TemplatePropertyValueCreator(resourceService, literalService, statementService)
        )
        return steps.execute(command, TemplatePropertyState()).templatePropertyId!!
    }

    private fun Resource.toTemplate(): Template {
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
        return Template(
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
            relations = TemplateRelation(
                researchFields = statements[id]!!
                    .wherePredicate(Predicates.templateOfResearchField)
                    .objectIdsAndLabel(),
                researchProblems = statements[id]!!
                    .wherePredicate(Predicates.templateOfResearchProblem)
                    .objectIdsAndLabel(),
                predicate = statements[id]!!
                    .wherePredicate(Predicates.templateOfPredicate)
                    .singleOrNull()
                    .objectIdAndLabel()
            ),
            properties = statements[id]!!
                .wherePredicate(Predicates.shProperty)
                .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
                .mapNotNull { (it.`object` as Resource).toTemplateProperty(statements[it.`object`.id].orEmpty()) }
                .sortedBy { it.order },
            isClosed = statements[id]!!
                .wherePredicate(Predicates.shClosed)
                .singleOrNull()
                .let { it?.`object`?.label.toBoolean() },
            createdAt = createdAt,
            createdBy = createdBy,
            organizations = listOf(organizationId),
            observatories = listOf(observatoryId),
            visibility = visibility,
            unlistedBy = unlistedBy
        )
    }

    private fun Resource.toTemplateProperty(statements: Iterable<GeneralStatement>): TemplateProperty? {
        val order = statements.wherePredicate(Predicates.shOrder).single().`object`.label.toLong()
        val minCount = statements.wherePredicate(Predicates.shMinCount).singleOrNull()?.`object`?.label?.toInt()
        val maxCount = statements.wherePredicate(Predicates.shMaxCount).singleOrNull()?.`object`?.label?.toInt()
        val pattern = statements.wherePredicate(Predicates.shPattern).singleOrNull()?.`object`?.label
        val path = statements.wherePredicate(Predicates.shPath).single().objectIdAndLabel()!!
        val datatype = statements.wherePredicate(Predicates.shDatatype).singleOrNull().objectIdAndLabel()
        val `class` = statements.wherePredicate(Predicates.shClass).singleOrNull().objectIdAndLabel()
        return when {
            datatype != null -> LiteralTemplateProperty(id, label, order, minCount, maxCount, pattern, path, createdBy, createdAt, datatype)
            `class` != null -> ResourceTemplateProperty(id, label, order, minCount, maxCount, pattern, path, createdBy, createdAt, `class`)
            else -> null
        }
    }
}
