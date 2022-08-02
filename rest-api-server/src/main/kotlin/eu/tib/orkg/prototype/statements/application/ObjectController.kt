package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.service.CreateObjectRequest
import eu.tib.orkg.prototype.statements.application.service.ObjectService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/objects/")
class ObjectController(
    private val objectService: ObjectService,
    private val resourceService: ResourceUseCases,
) : BaseController() {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        val resource = objectService.createObject(obj, authenticatedUserId())
        val location = uriComponentsBuilder.path("api/objects/").buildAndExpand(resource.id).toUri()
        return ResponseEntity.created(location).body(resource)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @PathVariable id: ResourceId,
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        val resource = objectService.createObject(obj, authenticatedUserId(), id)
        val location = uriComponentsBuilder.path("api/objects/").buildAndExpand(resource.id).toUri()
        return ResponseEntity.created(location).body(resource)
    }
}
