package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/resources/")
@CrossOrigin(origins = ["*"])
class ResourceController(private val repository: ResourceRepository) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ResourceId): Resource =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam(
            "q",
            required = false
        ) searchString: String?
    ) = if (searchString == null)
        repository.findAll()
    else
        repository.findByLabel(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody resource: Resource, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Resource> {
        val (id, resourceWithId) = if (resource.id == null) {
            val id = repository.nextIdentity()
            Pair(id, resource.copy(id = id))
        } else {
            Pair(resource.id, resource)
        }
        repository.add(resourceWithId)

        val location = uriComponentsBuilder
            .path("api/resources/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(repository.findById(id).get())
    }
}
