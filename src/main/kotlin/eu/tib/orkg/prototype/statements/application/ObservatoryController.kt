package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories/")
@CrossOrigin(origins = ["*"])
class ObservatoryController(
    private val service: ObservatoryService,
    private val userService: UserService,
    private val resourceService: ResourceService
) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (service.findByName(observatory.observatoryName).isEmpty) {
            val id = service.create(observatory.observatoryName, observatory.organizationId).id
            val location = uriComponentsBuilder
                .path("api/observatories/{id}")
                .buildAndExpand(id)
                .toUri()
            ResponseEntity.created(location).body(service.findById(id!!).get())
        } else
            ResponseEntity.badRequest().body("Observatory already exist")
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Observatory =
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }

    @GetMapping("/")
    fun findObservatories(): List<ObservatoryEntity> {
        return service.listObservatories()
    }

    @GetMapping("{id}/resources")
    fun findResourcesByObservatoryId(@PathVariable id: UUID): Iterable<Resource> {
        return resourceService.findAllByObservatoryId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByObservatoryId(@PathVariable id: UUID): Iterable<UserController.UserDetails> {
        return userService.findUsersByObservatoryId(id)
            .map(UserController::UserDetails)
    }

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: UUID
    )
}
