package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.*

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
        @RequestParam("q", required = false) searchString: String?
    ) =
        if (searchString == null)
            service.findAll()
        else
            service.findAllByLabelContaining(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody resource: Resource, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Resource> {
        val id = service.create(resource.label).id
        val location = uriComponentsBuilder
            .path("api/resources/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }
}
