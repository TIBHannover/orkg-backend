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
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
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
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val thingRepository: ThingRepository,
    private val classService: ClassUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val listService: ListUseCases,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository,
    private val classHierarchyRepository: ClassHierarchyRepository
) : TemplateInstanceUseCases {

    override fun findById(templateId: ThingId, id: ThingId, nested: Boolean): Optional<TemplateInstance> {
        val template = templateService.findById(templateId)
            .orElseThrow { TemplateNotFound(templateId) }
        return resourceRepository.findById(id).map {
            if (template.targetClass.id !in it.classes) {
                throw TemplateNotApplicable(template.id, id)
            }
            it.toTemplateInstance(template, nested)
        }
    }

    override fun findAll(
        templateId: ThingId,
        pageable: Pageable,
        nested: Boolean,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
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
            includeClasses = setOf(template.targetClass.id),
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
            TemplateInstancePropertyValueValidator(thingRepository, classRepository, statementRepository, classHierarchyRepository),
            TemplateInstanceSubjectUpdater(resourceRepository),
            TemplateInstancePropertyValueUpdater(classService, unsafeResourceUseCases, statementService, unsafeStatementUseCases, literalService, predicateService, statementRepository, listService)
        )
        steps.execute(command, UpdateTemplateInstanceState())
    }

    fun Resource.toTemplateInstance(template: Template, nested: Boolean = false): TemplateInstance {
        val statements = statementService.findAll(subjectId = id, pageable = PageRequests.ALL)
        val templateInstance = TemplateInstance(
            root = this,
            statements = associateTemplatePropertiesToStatements(template, statements)
        )
        if (nested) {
            val visitedThings = mutableSetOf(id)
            val visitedTemplates = mutableMapOf<ThingId, Template?>(template.id to template)
            return templateInstance.copy(
                statements = findNestedStatementsRecursively(
                    template = template,
                    statements = templateInstance.statements,
                    visitedThings = visitedThings,
                    visitedTemplates = visitedTemplates
                )
            )
        }
        return templateInstance
    }

    private fun associateTemplatePropertiesToStatements(
        template: Template,
        statements: Iterable<GeneralStatement>
    ): Map<ThingId, List<EmbeddedStatement>> =
        template.properties.associateBy(
            keySelector = { it.path.id },
            valueTransform = { property ->
                statements.filter { it.predicate.id == property.path.id }
                    .map { EmbeddedStatement(it.`object`, it.createdAt!!, it.createdBy, emptyMap()) }
            }
        )

    private fun findNestedStatementsRecursively(
        template: Template,
        statements: Map<ThingId, List<EmbeddedStatement>>,
        visitedThings: MutableSet<ThingId>,
        visitedTemplates: MutableMap<ThingId, Template?>
    ): Map<ThingId, List<EmbeddedStatement>> =
        template.properties.associateBy(
            keySelector = { it.path.id },
            valueTransform = { property ->
                val objects = statements[property.path.id].orEmpty()
                if (property !is ResourceTemplateProperty) {
                    return@associateBy objects
                }
                objects.map { `object` ->
                    if (`object`.thing.id in visitedThings || !`object`.thing.isInstanceOf(property.`class`.id)) {
                        return@map `object`
                    }
                    visitedThings += `object`.thing.id
                    val nestedTemplate = visitedTemplates.getOrPut(property.`class`.id) {
                        templateService.findAll(
                            targetClass = property.`class`.id,
                            pageable = PageRequests.SINGLE
                        ).singleOrNull()
                    }
                    if (nestedTemplate == null) {
                        return@map `object`
                    }
                    val nestedStatements = statementService.findAll(
                        subjectId = `object`.thing.id,
                        pageable = PageRequests.ALL
                    )
                    val associatedStatements = associateTemplatePropertiesToStatements(
                        template = nestedTemplate,
                        statements = nestedStatements.content
                    )
                    val recursiveStatements = findNestedStatementsRecursively(
                        template = nestedTemplate,
                        statements = associatedStatements,
                        visitedThings = visitedThings,
                        visitedTemplates = visitedTemplates
                    )
                    `object`.copy(statements = recursiveStatements)
                }
            }
        )

    private infix fun Thing.isInstanceOf(id: ThingId): Boolean =
        this is Resource && id in classes
}
