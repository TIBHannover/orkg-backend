package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ResourceContributors
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
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
@RequestMapping("/api/resources/")
class ResourceController(
    private val service: ResourceService,
    private val contributorService: ContributorService
) : BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ResourceId): Resource =
        service
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("exclude", required = false, defaultValue = "") excludeClasses: Array<String>,
        pageable: Pageable
    ): Page<Resource> {
        return when {
            excludeClasses.isNotEmpty() -> when {
                searchString == null -> service.findAllExcludingClass(pageable, excludeClasses.map { ClassId(it) }.toTypedArray())
                exactMatch -> service.findAllExcludingClassByLabel(pageable, excludeClasses.map { ClassId(it) }.toTypedArray(), searchString)
                else -> service.findAllExcludingClassByLabelContaining(pageable, excludeClasses.map { ClassId(it) }.toTypedArray(), searchString)
            }
            else -> when {
                searchString == null -> service.findAll(pageable)
                exactMatch -> service.findAllByLabel(pageable, searchString)
                else -> service.findAllByLabelContaining(pageable, searchString)
            }
        }
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody resource: CreateResourceRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        if (resource.id != null && service.findById(resource.id).isPresent)
            return badRequest().body("Resource id <${resource.id}> already exists!")
        val userId = authenticatedUserId()
        val contributor = contributorService.findById(ContributorId(userId))
        var observatoryId = ObservatoryId.createUnknownObservatory()
        var organizationId = OrganizationId.createUnknownOrganization()
        if (!contributor.isEmpty) {
            organizationId = contributor.get().organizationId
            observatoryId = contributor.get().observatoryId
        }
        val id = service.create(ContributorId(userId), resource, observatoryId, resource.extractionMethod, organizationId).id
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
    ): ResponseEntity<Resource> {
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
    ): ResponseEntity<Resource> {
        val found = service.findById(id)
        if (!found.isPresent)
            return notFound().build()
        return ok(service.updatePaperObservatory(request, id))
    }

    @GetMapping("{id}/contributors")
    fun findContributorsById(@PathVariable id: ResourceId): Iterable<ResourceContributors> {
        return service.findContributorsByResourceId(id)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun delete(@PathVariable id: ResourceId): ResponseEntity<Unit> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        if (service.hasStatements(found.get().id!!))
            throw ResourceCantBeDeleted(id)

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
        service.markAsFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
    @DeleteMapping("/{id}/metadata/featured")
    fun unmarkFeatured(@PathVariable id: ResourceId) {
        service.markAsNonFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ResourceId): Boolean =
        service.getFeaturedResourceFlag(id) ?: throw ResourceNotFound(id.toString())

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedResources(pageable: Pageable) =
        service.findAllByUnlisted(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedResources(pageable: Pageable) =
        service.findAllByListed(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ResourceId) {
        service.markAsUnlisted(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
    @DeleteMapping("/{id}/metadata/unlisted")
    fun unmarkUnlisted(@PathVariable id: ResourceId) {
        service.markAsListed(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ResourceId): Boolean =
        service.getUnlistedResourceFlag(id) ?: throw ResourceNotFound(id.toString())
}

enum class ExtractionMethod {
    AUTOMATIC,
    MANUAL,
    UNKNOWN
}

data class CreateResourceRequest(
    val id: ResourceId?,
    val label: String,
    val classes: Set<ClassId> = emptySet(),
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
)

data class UpdateResourceRequest(
    val id: ResourceId?,
    val label: String?,
    val classes: Set<ClassId>?
)

data class UpdateResourceObservatoryRequest(
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId,
    @JsonProperty("organization_id")
    val organizationId: OrganizationId
)
