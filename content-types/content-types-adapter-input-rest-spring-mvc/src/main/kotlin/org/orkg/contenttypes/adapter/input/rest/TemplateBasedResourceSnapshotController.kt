package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateBasedResourceSnapshotRepresentationAdapter
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
import org.orkg.contenttypes.input.CreateTemplateBasedResourceSnapshotUseCase
import org.orkg.contenttypes.input.TemplateBasedResourceSnapshotUseCases
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.status
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/resources/{id}/snapshots")
class TemplateBasedResourceSnapshotController(
    private val service: TemplateBasedResourceSnapshotUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
    @Value("\${orkg.snapshots.resources.url-templates.frontend}")
    private val frontendUrlTemplate: String,
) : TemplateBasedResourceSnapshotRepresentationAdapter {
    @GetMapping("/{snapshotId}", produces = [APPLICATION_JSON_VALUE])
    fun findById(
        @PathVariable(name = "id") resourceId: ThingId,
        @PathVariable snapshotId: SnapshotId,
        capabilities: MediaTypeCapabilities,
    ): TemplateBasedResourceSnapshotRepresentation =
        service.findById(snapshotId)
            .filter { it.resourceId == resourceId }
            .mapToTemplateBasedResourceSnapshotRepresentation(capabilities)
            .orElseThrow { TemplateBasedResourceSnapshotNotFound(snapshotId) }

    @GetMapping("/{snapshotId}", produces = [MediaType.ALL_VALUE, MediaType.TEXT_HTML_VALUE])
    fun redirectById(
        @PathVariable(name = "id") resourceId: ThingId,
        @PathVariable snapshotId: SnapshotId,
        uriComponentsBuilder: UriComponentsBuilder,
        capabilities: MediaTypeCapabilities,
    ): ResponseEntity<Any> {
        val location = UriComponentsBuilder.fromUriString(frontendUrlTemplate)
            .build(resourceId, snapshotId)
        return status(HttpStatus.PERMANENT_REDIRECT)
            .location(location)
            .build()
    }

    @GetMapping
    fun findAll(
        @PathVariable(name = "id") resourceId: ThingId,
        @RequestParam("template_id", required = false) templateId: ThingId?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<TemplateBasedResourceSnapshotRepresentation> =
        when {
            templateId == null -> service.findAllByResourceId(resourceId, pageable)
                .mapToTemplateBasedResourceSnapshotRepresentation(capabilities)
            else -> service.findAllByResourceIdAndTemplateId(resourceId, templateId, pageable)
                .mapToTemplateBasedResourceSnapshotRepresentation(capabilities)
        }

    @RequireLogin
    @PostMapping
    fun create(
        @PathVariable(name = "id") resourceId: ThingId,
        @RequestBody @Valid request: CreateTemplateBasedResourceSnapshotRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId, resourceId))
        val location = uriComponentsBuilder
            .path("/api/resources/{id}/snapshots/{snapshotId}")
            .buildAndExpand(resourceId, id)
            .toUri()
        return created(location).build()
    }

    data class CreateTemplateBasedResourceSnapshotRequest(
        @JsonProperty("template_id")
        val templateId: ThingId,
        @JsonProperty("register_handle")
        val registerHandle: Boolean = true,
    ) {
        fun toCreateCommand(contributorId: ContributorId, resourceId: ThingId) =
            CreateTemplateBasedResourceSnapshotUseCase.CreateCommand(
                resourceId = resourceId,
                templateId = templateId,
                contributorId = contributorId,
                registerHandle = registerHandle
            )
    }
}
