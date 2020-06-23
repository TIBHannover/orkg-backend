package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ResourceContributors
import java.util.Optional
import java.util.UUID
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/resources/")
class ResourceController(
    private val service: ResourceService,
    private val userService: UserService,
    private val statementService: StatementService
) : BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ResourceId): Resource {
        return service
            .findById(id)
            .orElseThrow { ResourceNotFound() }
    }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean,
        @RequestParam("exclude", required = false, defaultValue = "") excludeClasses: Array<String>
    ): Iterable<Resource> {
        val pagination = createPageable(page, items, sortBy, desc)
        return when {
            excludeClasses.isNotEmpty() -> when {
                searchString == null -> service.findAllExcludingClass(pagination, excludeClasses.map { ClassId(it) }.toTypedArray())
                exactMatch -> service.findAllExcludingClassByLabel(pagination, excludeClasses.map { ClassId(it) }.toTypedArray(), searchString)
                else -> service.findAllExcludingClassByLabelContaining(pagination, excludeClasses.map { ClassId(it) }.toTypedArray(), searchString)
            }
            else -> when {
                searchString == null -> service.findAll(pagination)
                exactMatch -> service.findAllByLabel(pagination, searchString)
                else -> service.findAllByLabelContaining(pagination, searchString)
            }
        }
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody resource: CreateResourceRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        if (resource.id != null && service.findById(resource.id).isPresent)
            return badRequest().body("Resource id <${resource.id}> already exists!")
        val userId = authenticatedUserId()
        val user: Optional<UserEntity> = userService.findById(userId)
        var observatoryId = UUID(0, 0)
        var organizationId = UUID(0, 0)
        if (user.isPresent) {
            organizationId = user.get().organizationId ?: UUID(0, 0)
            observatoryId = user.get().observatoryId ?: UUID(0, 0)
        }

        val id = service.create(userId, resource, observatoryId, resource.extractionMethod, organizationId).id
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

    @GetMapping("{id}/contributors")
    fun findContributorsById(@PathVariable id: ResourceId): Iterable<ResourceContributors> {
        return service.findContributorsByResourceId(id)
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
    val classes: Set<ClassId> = emptySet(),
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
)

data class UpdateResourceRequest(
    val id: ResourceId?,
    val label: String?,
    val classes: Set<ClassId>?
)
