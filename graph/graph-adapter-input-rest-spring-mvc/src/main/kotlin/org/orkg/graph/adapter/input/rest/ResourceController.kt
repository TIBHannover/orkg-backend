package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import dev.forkhandles.values.ofOrNull
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeCurator
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.ResourceAlreadyExists
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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

@RestController
@RequestMapping("/api/resources/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResourceController(
    private val service: ResourceUseCases,
    private val contributorService: RetrieveContributorUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): ResourceRepresentation =
        service.findById(id).mapToResourceRepresentation().orElseThrow { ResourceNotFound.withId(id) }

    @GetMapping("/")
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("include", required = false, defaultValue = "") includeClasses: Set<ThingId>,
        @RequestParam("exclude", required = false, defaultValue = "") excludeClasses: Set<ThingId>,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable
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
            observatoryId = observatoryId,
            organizationId = organizationId
        ).mapToResourceRepresentation()

    @PreAuthorizeUser
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody resource: CreateResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<ResourceRepresentation> {
        Label.ofOrNull(resource.label) ?: throw InvalidLabel()
        if (resource.id != null && service.findById(resource.id).isPresent) throw ResourceAlreadyExists(resource.id)
        val contributor = contributorService.findById(currentUser.contributorId())
        var observatoryId = ObservatoryId.createUnknownObservatory()
        var organizationId = OrganizationId.createUnknownOrganization()
        if (!contributor.isEmpty) {
            organizationId = contributor.get().organizationId
            observatoryId = contributor.get().observatoryId
        }
        val id =
            service.create(
                CreateResourceUseCase.CreateCommand(
                    id = resource.id,
                    label = resource.label,
                    classes = resource.classes,
                    extractionMethod = resource.extractionMethod,
                    contributorId = currentUser.contributorId(),
                    observatoryId = observatoryId,
                    organizationId = organizationId,
                )
            )

        val location = uriComponentsBuilder
            .path("api/resources/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).mapToResourceRepresentation().get())
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody request: UpdateResourceRequest
    ): ResponseEntity<ResourceRepresentation> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        service.update(
            UpdateResourceUseCase.UpdateCommand(
                id = id,
                label = request.label,
                classes = request.classes,
                extractionMethod = request.extractionMethod
            )
        )
        return ok(service.findById(id).mapToResourceRepresentation().get())
    }

    @RequestMapping("{id}/observatory", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorizeCurator
    fun updateWithObservatory(
        @PathVariable id: ThingId,
        @RequestBody request: UpdateResourceObservatoryRequest
    ): ResponseEntity<ResourceRepresentation> {
        val found = service.findById(id)
        if (!found.isPresent)
            return notFound().build()
        service.update(
            UpdateResourceUseCase.UpdateCommand(
                id = id,
                organizationId = request.organizationId,
                observatoryId = request.observatoryId,
            )
        )
        return ok(service.findById(id).mapToResourceRepresentation().get())
    }

    @GetMapping("{id}/contributors")
    fun findContributorsById(@PathVariable id: ThingId, pageable: Pageable): Page<ContributorId> =
        service.findAllContributorsByResourceId(id, pageable)

    @GetMapping("{id}/timeline")
    fun findTimelineById(@PathVariable id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        service.findTimelineByResourceId(id, pageable)

    @DeleteMapping("/{id}")
    @PreAuthorizeCurator
    fun delete(@PathVariable id: ThingId): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedResources(pageable: Pageable) =
        service.findAllByVisibility(VisibilityFilter.FEATURED, pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedResources(pageable: Pageable) =
        service.findAllByVisibility(VisibilityFilter.NON_FEATURED, pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorizeCurator
    fun markFeatured(@PathVariable id: ThingId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @PreAuthorizeCurator
    fun unmarkFeatured(@PathVariable id: ThingId) {
        service.markAsNonFeatured(id)
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ThingId): Boolean =
        service.findById(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedResources(pageable: Pageable) =
        service.findAllByVisibility(VisibilityFilter.UNLISTED, pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedResources(pageable: Pageable) =
        service.findAllByVisibility(VisibilityFilter.ALL_LISTED, pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorizeCurator
    fun markUnlisted(@PathVariable id: ThingId, @AuthenticationPrincipal currentUser: UserDetails?) {
        service.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @PreAuthorizeCurator
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        service.markAsListed(id)
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ThingId): Boolean =
        service.findById(id)
            .map { it.visibility == Visibility.UNLISTED }
            .orElseThrow { ResourceNotFound.withId(id) }

    @GetMapping("/classes")
    fun getResourcesByClass(
        @RequestParam(value = "classes") classes: Set<ThingId>,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        return service.findAllByClassInAndVisibility(
            classes = classes,
            visibility = visibility ?: visibilityFilterFromFlags(featured, unlisted),
            pageable = pageable
        ).mapToResourceRepresentation()
    }
}

data class CreateResourceRequest(
    val id: ThingId?,
    val label: String,
    val classes: Set<ThingId> = emptySet(),
    @JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
)

data class UpdateResourceRequest(
    val id: ThingId?,
    val label: String?,
    val classes: Set<ThingId>?,
    @JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod?
)

data class UpdateResourceObservatoryRequest(
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    @JsonProperty("organization_id")
    val organizationId: OrganizationId
)

fun visibilityFilterFromFlags(featured: Boolean?, unlisted: Boolean?): VisibilityFilter =
    when (unlisted ?: false) {
        true -> VisibilityFilter.UNLISTED
        false -> when (featured) {
            null -> VisibilityFilter.ALL_LISTED
            true -> VisibilityFilter.FEATURED
            false -> VisibilityFilter.NON_FEATURED
        }
    }
