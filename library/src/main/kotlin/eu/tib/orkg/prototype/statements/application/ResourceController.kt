package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.lang.Nullable
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
@RequestMapping("/api/resources/")
class ResourceController(
    private val service: ResourceUseCases,
    private val contributorService: ContributorService
) : BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ResourceId): ResourceRepresentation =
        service.findById(id).orElseThrow { ResourceNotFound(id) }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("include", required = false, defaultValue = "") includeClasses: Set<ThingId>,
        @RequestParam("exclude", required = false, defaultValue = "") excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        when {
            excludeClasses.isNotEmpty() || includeClasses.isNotEmpty() -> when {
                searchString == null -> service.findAllIncludingAndExcludingClasses(
                    includeClasses,
                    excludeClasses,
                    pageable
                )
                exactMatch -> service.findAllIncludingAndExcludingClassesByLabel(
                    includeClasses,
                    excludeClasses,
                    searchString,
                    pageable
                )
                else -> service.findAllIncludingAndExcludingClassesByLabelContaining(
                    includeClasses,
                    excludeClasses,
                    searchString,
                    pageable
                )
            }
            else -> when {
                searchString == null -> service.findAll(pageable)
                exactMatch -> service.findAllByLabel(pageable, searchString)
                else -> service.findAllByLabelContaining(pageable, searchString)
            }
        }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody resource: CreateResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
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
            service.create(ContributorId(userId), resource, observatoryId, resource.extractionMethod, organizationId).id
        val location = uriComponentsBuilder
            .path("api/resources/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: ResourceId,
        @RequestBody request: UpdateResourceRequest
    ): ResponseEntity<ResourceRepresentation> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        val updatedRequest = request.copy(id = id)

        return ok(service.update(updatedRequest))
    }

    @RequestMapping("{id}/observatory", method = [RequestMethod.POST, RequestMethod.PUT])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateWithObservatory(
        @PathVariable id: ResourceId,
        @RequestBody request: UpdateResourceObservatoryRequest
    ): ResponseEntity<ResourceRepresentation> {
        val found = service.findById(id)
        if (!found.isPresent)
            return notFound().build()
        return ok(service.updatePaperObservatory(request, id))
    }

    @GetMapping("{id}/contributors")
    fun findContributorsById(@PathVariable id: ResourceId, pageable: Pageable): Page<ResourceContributor> {
        return service.findContributorsByResourceId(id, pageable)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun delete(@PathVariable id: ResourceId): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedResources(pageable: Pageable) =
        service.findAllByFeatured(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedResources(pageable: Pageable) =
        service.findAllByNonFeatured(pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(@PathVariable id: ResourceId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    fun unmarkFeatured(@PathVariable id: ResourceId) {
        service.markAsNonFeatured(id)
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ResourceId): Boolean? =
        service.getFeaturedResourceFlag(id)

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedResources(pageable: Pageable) =
        service.findAllByUnlisted(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedResources(pageable: Pageable) =
        service.findAllByListed(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ResourceId) {
        service.markAsUnlisted(id)
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    fun unmarkUnlisted(@PathVariable id: ResourceId) {
        service.markAsListed(id)
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ResourceId): Boolean = service.getUnlistedResourceFlag(id)

    @GetMapping("/classes")
    fun getResourcesByClass(
        @RequestParam(value = "classes") classes: List<String>,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        return service.getResourcesByClasses(classes, featured, unlisted, pageable)
    }
}

enum class ExtractionMethod {
    AUTOMATIC,
    MANUAL,
    UNKNOWN
}

data class CreateResourceRequest(
    val id: ResourceId?,
    val label: String,
    val classes: Set<ThingId> = emptySet(),
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
)

data class UpdateResourceRequest(
    val id: ResourceId?,
    val label: String?,
    val classes: Set<ThingId>?
)

data class UpdateResourceObservatoryRequest(
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    @JsonProperty("organization_id")
    val organizationId: OrganizationId
)
