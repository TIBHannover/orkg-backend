package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ResourceRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.UpdateResourceUseCase
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
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
    private val contributorService: ContributorService,
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : BaseController(), ResourceRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): ResourceRepresentation =
        service.findById(id).mapToResourceRepresentation().orElseThrow { ResourceNotFound.withId(id) }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("include", required = false, defaultValue = "") includeClasses: Set<ThingId>,
        @RequestParam("exclude", required = false, defaultValue = "") excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        when {
            excludeClasses.isNotEmpty() || includeClasses.isNotEmpty() -> when (string) {
                null -> service.findAllIncludingAndExcludingClasses(
                    includeClasses,
                    excludeClasses,
                    pageable
                )
                else -> service.findAllIncludingAndExcludingClassesByLabel(
                    includeClasses,
                    excludeClasses,
                    SearchString.of(string, exactMatch),
                    pageable
                )
            }
            else -> when (string) {
                null -> service.findAll(pageable)
                else -> service.findAllByLabel(SearchString.of(string, exactMatch), pageable)
            }
        }.mapToResourceRepresentation()

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody resource: CreateResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        Label.ofOrNull(resource.label) ?: throw InvalidLabel()
        if (resource.id != null && service.findById(resource.id).isPresent) throw ResourceAlreadyExists(resource.id)
        val userId = authenticatedUserId()
        val contributor = contributorService.findById(ContributorId(userId))
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
                    contributorId = ContributorId(userId),
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
            )
        )
        return ok(service.findById(id).mapToResourceRepresentation().get())
    }

    @RequestMapping("{id}/observatory", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun markFeatured(@PathVariable id: ThingId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    fun markUnlisted(@PathVariable id: ThingId) {
        service.markAsUnlisted(id)
    }

    @DeleteMapping("/{id}/metadata/unlisted")
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
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
)

data class UpdateResourceRequest(
    val id: ThingId?,
    val label: String?,
    val classes: Set<ThingId>?
)

data class UpdateResourceObservatoryRequest(
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    @JsonProperty("organization_id")
    val organizationId: OrganizationId
)

internal fun visibilityFilterFromFlags(featured: Boolean?, unlisted: Boolean?): VisibilityFilter =
    when (unlisted ?: false) {
        true -> VisibilityFilter.UNLISTED
        false -> when (featured) {
            null -> VisibilityFilter.ALL_LISTED
            true -> VisibilityFilter.FEATURED
            false -> VisibilityFilter.NON_FEATURED
        }
    }
