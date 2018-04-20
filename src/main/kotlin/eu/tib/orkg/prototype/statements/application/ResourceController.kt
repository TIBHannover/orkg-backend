package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements/resources")
class ResourceController(private val repository: ResourceRepository) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ResourceId): Resource =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findAll(): Iterable<ResourceId> = repository.findAll()

    @PostMapping("/")
    fun add(@RequestBody resource: Resource) = repository.add(resource)
}
