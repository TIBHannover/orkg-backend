package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ResourceRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.CreateObjectUseCase.*
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ObjectService
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
@RequestMapping("/api/objects/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObjectController(
    private val resourceService: ResourceUseCases,
    private val service: ObjectService,
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService,
) : BaseController(), ResourceRepresentationAdapter {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        val id = service.createObject(obj, null, authenticatedUserId())
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(resourceService.findById(id).mapToResourceRepresentation().get())
    }

    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @PathVariable id: ThingId,
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ResourceRepresentation> {
        resourceService
            .findById(id)
            .orElseThrow { ResourceNotFound.withId(id) }
        service.createObject(obj, id, authenticatedUserId())
        val location = uriComponentsBuilder
            .path("api/objects/")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(resourceService.findById(id).mapToResourceRepresentation().get())
    }
}