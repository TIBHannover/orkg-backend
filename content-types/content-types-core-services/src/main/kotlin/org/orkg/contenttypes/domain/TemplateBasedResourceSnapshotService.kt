package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.Handle
import org.orkg.contenttypes.input.CreateTemplateBasedResourceSnapshotUseCase
import org.orkg.contenttypes.input.TemplateBasedResourceSnapshotUseCases
import org.orkg.contenttypes.input.TemplateInstanceUseCases
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.output.HandleService
import org.orkg.contenttypes.output.TemplateBasedResourceSnapshotRepository
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class TemplateBasedResourceSnapshotService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val templateUseCase: TemplateUseCases,
    private val templateInstanceUseCases: TemplateInstanceUseCases,
    private val templateBasedResourceSnapshotRepository: TemplateBasedResourceSnapshotRepository,
    private val handleService: HandleService,
    private val snapshotIdGenerator: SnapshotIdGenerator,
    @Value("\${orkg.snapshots.resources.url-templates.backend}")
    private val urlTemplate: String,
    private val clock: Clock,
) : TemplateBasedResourceSnapshotUseCases {
    override fun findById(snapshotId: SnapshotId): Optional<TemplateBasedResourceSnapshot<*>> =
        templateBasedResourceSnapshotRepository.findById(snapshotId)

    override fun findAllByResourceId(resourceId: ThingId, pageable: Pageable): Page<TemplateBasedResourceSnapshot<*>> =
        templateBasedResourceSnapshotRepository.findAllByResourceId(resourceId, pageable)

    override fun findAllByResourceIdAndTemplateId(
        resourceId: ThingId,
        templateId: ThingId,
        pageable: Pageable,
    ): Page<TemplateBasedResourceSnapshot<*>> =
        templateBasedResourceSnapshotRepository.findAllByResourceIdAndTemplateId(resourceId, templateId, pageable)

    override fun create(command: CreateTemplateBasedResourceSnapshotUseCase.CreateCommand): SnapshotId {
        val resource = resourceRepository.findById(command.resourceId)
            .orElseThrow { ResourceNotFound.withId(command.resourceId) }
        val template = templateUseCase.findById(command.templateId)
            .orElseThrow { TemplateNotFound(command.templateId) }
        if (template.targetClass.id !in resource.classes) {
            throw TemplateNotApplicable(template.id, resource.id)
        }
        val templateInstance = templateInstanceUseCases.findById(template.id, resource.id)
            .orElseThrow { TemplateInstanceNotFound(template.id, resource.id) }
        val id = snapshotIdGenerator.nextIdentity()
        var handle: Handle? = null
        if (command.registerHandle) {
            val url = UriComponentsBuilder.fromUriString(urlTemplate)
                .build(resource.id, id)
            val createHandleCommand = HandleService.RegisterCommand(id.value, url)
            handle = handleService.register(createHandleCommand)
        }
        val templateSnapshot = TemplateBasedResourceSnapshotV1(
            id = id,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
            data = templateInstance,
            resourceId = resource.id,
            templateId = template.id,
            handle = handle
        )
        templateBasedResourceSnapshotRepository.save(templateSnapshot)
        return id
    }
}
