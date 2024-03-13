package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstancePropertyValueUpdater
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstancePropertyValueValidator
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstanceSubjectUpdater
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstanceSubjectValidator
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstanceTempIdValidator
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstanceTemplateValidator
import org.orkg.contenttypes.domain.actions.templates.instances.TemplateInstanceThingDefinitionValidator
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.input.UpdateTemplateInstanceUseCase
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class TemplateInstanceService(
    private val resourceRepository: ResourceRepository,
    private val templateService: TemplateUseCases,
    private val statementService: StatementUseCases,
    private val thingRepository: ThingRepository,
    private val resourceService: ResourceUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val listService: ListUseCases,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository
) : TemplateInstanceUseCases {

    override fun findById(templateId: ThingId, id: ThingId): Optional<TemplateInstance> {
        val template = templateService.findById(templateId)
            .orElseThrow { TemplateNotFound(templateId) }
        return resourceRepository.findById(id).map {
            if (template.targetClass !in it.classes) {
                throw TemplateNotApplicable(template.id, id)
            }
            it.toTemplateInstance(template)
        }
    }

    override fun findAll(
        templateId: ThingId,
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?
    ): Page<TemplateInstance> {
        val template = templateService.findById(templateId)
            .orElseThrow { TemplateNotFound(templateId) }
        return resourceRepository.findAll(
            pageable = pageable,
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            includeClasses = setOf(template.targetClass),
            observatoryId = observatoryId,
            organizationId = organizationId,
        ).pmap { it.toTemplateInstance(template) }
    }

    override fun update(command: UpdateTemplateInstanceUseCase.UpdateCommand) {
        val steps = listOf(
            TemplateInstanceTempIdValidator(),
            TemplateInstanceTemplateValidator(templateService),
            TemplateInstanceSubjectValidator(resourceRepository, this),
            TemplateInstanceThingDefinitionValidator(thingRepository, classRepository),
            TemplateInstancePropertyValueValidator(thingRepository, classRepository),
            TemplateInstanceSubjectUpdater(resourceRepository),
            TemplateInstancePropertyValueUpdater(resourceService, statementService, literalService, predicateService, statementRepository, listService)
        )
        steps.execute(command, UpdateTemplateInstanceState())
    }

    fun Resource.toTemplateInstance(template: Template): TemplateInstance {
        val statements = statementService.findAll(subjectId = id, pageable = PageRequests.ALL)
        return TemplateInstance(
            root = this,
            statements = template.properties.associateBy(
                keySelector = { it.path.id },
                valueTransform = { property ->
                    statements.content
                        .filter { it.predicate.id == property.path.id }
                        .map { EmbeddedStatement(it.`object`, it.createdAt!!, it.createdBy, emptyMap()) }
                }
            )
        )
    }
}
