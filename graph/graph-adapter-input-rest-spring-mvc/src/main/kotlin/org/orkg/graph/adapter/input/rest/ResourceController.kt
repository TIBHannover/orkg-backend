package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

@RestController
@RequestMapping("/api/resources", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResourceController(
    private val service: ResourceUseCases,
    private val contributorService: RetrieveContributorUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
        capabilities: MediaTypeCapabilities,
    ): ResourceRepresentation =
        service.findById(id).mapToResourceRepresentation(capabilities).orElseThrow { ResourceNotFound.withId(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("include", required = false, defaultValue = "") includeClasses: Set<ThingId>,
        @RequestParam("exclude", required = false, defaultValue = "") excludeClasses: Set<ThingId>,
        @RequestParam("base_class", required = false) baseClass: ThingId?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> =
        service.findAll(
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            includeClasses = includeClasses,
            excludeClasses = excludeClasses,
            baseClass = baseClass,
            observatoryId = observatoryId,
            organizationId = organizationId
        ).mapToResourceRepresentation(capabilities)

    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody request: CreateResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
        capabilities: MediaTypeCapabilities,
    ): ResponseEntity<ResourceRepresentation> {
        val contributor = contributorService.findById(currentUser.contributorId())
        val id = service.create(
            CreateResourceUseCase.CreateCommand(
                id = request.id,
                contributorId = currentUser.contributorId(),
                label = request.label,
                classes = request.classes,
                extractionMethod = request.extractionMethod,
                observatoryId = contributor.map { it.observatoryId }.orElse(ObservatoryId.UNKNOWN),
                organizationId = contributor.map { it.organizationId }.orElse(OrganizationId.UNKNOWN),
            )
        )
        val location = uriComponentsBuilder
            .path("/api/resources/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).body(service.findById(id).mapToResourceRepresentation(capabilities).get())
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody request: UpdateResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        capabilities: MediaTypeCapabilities,
        currentUser: Authentication?,
    ): ResponseEntity<ResourceRepresentation> {
        service.update(request.toUpdateCommand(id, currentUser.contributorId()))
        val location = uriComponentsBuilder
            .path("/api/resources/{id}")
            .buildAndExpand(id)
            .toUri()
        return ok().location(location).body(service.findById(id).mapToResourceRepresentation(capabilities).get())
    }

    @Deprecated("To be removed", replaceWith = ReplaceWith("update"))
    @RequireLogin
    @RequestMapping("{id}/observatory", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateWithObservatory(
        @PathVariable id: ThingId,
        @RequestBody request: UpdateResourceObservatoryRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        capabilities: MediaTypeCapabilities,
        currentUser: Authentication?,
    ): ResponseEntity<ResourceRepresentation> = update(
        id = id,
        request = UpdateResourceRequest(
            id = id,
            label = null,
            classes = null,
            organizationId = request.organizationId,
            observatoryId = request.observatoryId,
            extractionMethod = null,
            visibility = null
        ),
        uriComponentsBuilder = uriComponentsBuilder,
        capabilities = capabilities,
        currentUser = currentUser
    )

    @GetMapping("/{id}/contributors")
    fun findContributorsById(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<ContributorId> =
        service.findAllContributorsByResourceId(id, pageable)

    @GetMapping("/{id}/timeline")
    fun findTimelineById(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<ResourceContributor> =
        service.findTimelineByResourceId(id, pageable)

    @DeleteMapping("/{id}")
    @RequireLogin
    fun delete(
        @PathVariable id: ThingId,
        currentUser: Authentication?,
    ): ResponseEntity<Unit> {
        service.delete(id, currentUser.contributorId())
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    @RequireCuratorRole
    fun markFeatured(
        @PathVariable id: ThingId,
    ) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    fun unmarkFeatured(
        @PathVariable id: ThingId,
    ) {
        service.markAsNonFeatured(id)
    }

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    @RequireCuratorRole
    fun markUnlisted(
        @PathVariable id: ThingId,
        currentUser: Authentication?,
    ) {
        service.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    fun unmarkUnlisted(
        @PathVariable id: ThingId,
    ) {
        service.markAsListed(id)
    }

    @GetMapping("/classes")
    fun getResourcesByClass(
        @RequestParam(value = "classes") classes: Set<ThingId>,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> = service.findAllByClassInAndVisibility(
        classes = classes,
        visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
        pageable = pageable
    ).mapToResourceRepresentation(capabilities)

    data class CreateResourceRequest(
        val id: ThingId?,
        val label: String,
        val classes: Set<ThingId> = emptySet(),
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    )

    data class UpdateResourceRequest(
        val id: ThingId?,
        val label: String?,
        val classes: Set<ThingId>?,
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId?,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
    ) {
        fun toUpdateCommand(id: ThingId, contributorId: ContributorId): UpdateResourceUseCase.UpdateCommand =
            UpdateResourceUseCase.UpdateCommand(
                id = id,
                contributorId = contributorId,
                label = label,
                classes = classes,
                observatoryId = observatoryId,
                organizationId = organizationId,
                extractionMethod = extractionMethod,
                visibility = visibility
            )
    }

    data class UpdateResourceObservatoryRequest(
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId,
    )
}
