package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
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
import java.net.URI

@RestController
@RequestMapping("/api/classes/")
@CrossOrigin(origins = ["*"])
class ClassController(private val service: ClassService, private val resourceService: ResourceService) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ClassId): Class =
        service
            .findById(id)
            .orElseThrow { ClassNotFound() }

    @GetMapping("/{id}/resources/")
    fun findResourcesWithClass(
        @PathVariable id: ClassId,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): Iterable<Resource> {
        val pagination = createPageable(page, items, sortBy, desc)
        return resourceService.findAllByClass(pagination, id)
        // if (result.none()) throw ResourceNotFound()
    }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean
    ) =
        if (searchString == null)
            service.findAll()
        else if (exactMatch)
            service.findAllByLabel(searchString)
        else
            service.findAllByLabelContaining(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody `class`: CreateClassRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Class> {
        val id = service.create(`class`).id
        val location = uriComponentsBuilder
            .path("api/classes/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: ClassId,
        @RequestBody `class`: Class
    ): ResponseEntity<Class> {
        val found = service.findById(id)

        if (!found.isPresent)
            return ResponseEntity.notFound().build()

        val updatedClass = `class`.copy(id = found.get().id)

        return ResponseEntity.ok(service.update(updatedClass))
    }
}

data class CreateClassRequest(
    val id: ClassId?,
    val label: String,
    val uri: URI?
)
