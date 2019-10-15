package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
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
@CrossOrigin(origins = ["*"])
class ResourceController(private val service: ResourceService) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ResourceId): Resource =
        service
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): Iterable<Resource> {
        val pagination = createPageable(page, items, sortBy, desc)
        if (searchString == null)
            return service.findAll(pagination)
        else if (exactMatch)
            return service.findAllByLabel(pagination, searchString)
        else
            return service.findAllByLabelContaining(pagination, searchString)
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody resource: CreateResourceRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Resource> {
        val id = service.create(resource).id
        val location = uriComponentsBuilder
            .path("api/resources/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: ResourceId,
        @RequestBody resource: Resource
    ): ResponseEntity<Resource> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        val updatedResource = resource.copy(id = found.get().id)

        return ok(service.update(updatedResource))
    }
}

data class CreateResourceRequest(
    val id: ResourceId?,
    val label: String,
    val classes: Set<ClassId> = emptySet()
)
