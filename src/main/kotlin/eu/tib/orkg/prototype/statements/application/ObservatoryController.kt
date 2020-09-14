package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories/")
@CrossOrigin(origins = ["*"])
class ObservatoryController(
    private val service: ObservatoryService,
    private val userService: UserService,
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService
) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (service.findByName(observatory.observatoryName).isEmpty) {
            var organizationEntity = organizationService.findById(observatory.organizationId)
            val id = service.create(observatory.observatoryName, observatory.description, organizationEntity.get(), observatory.researchField).id
            val location = uriComponentsBuilder
                .path("api/observatories/{id}")
                .buildAndExpand(id)
                .toUri()
            ResponseEntity.created(location).body(service.findById(id!!).get())
        } else
            ResponseEntity.badRequest().body(
                    ErrorMessage(message = "Observatory already exist")
                )
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): Observatory =
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }

    @GetMapping("/")
    fun findObservatories(): List<Observatory> {
        return service.listObservatories()
    }

    @GetMapping("{id}/papers")
    fun findPapersByObservatoryId(@PathVariable id: UUID): Iterable<Resource> {
        return resourceService.findPapersByObservatoryId(id)
    }

    @GetMapping("{id}/comparisons")
    fun findComparisonsByObservatoryId(@PathVariable id: UUID): Iterable<Resource> {
        return resourceService.findComparisonsByObservatoryId(id)
    }

    @GetMapping("{id}/problems")
    fun findProblemsByObservatoryId(@PathVariable id: UUID): Iterable<Resource> {
        return resourceService.findProblemsByObservatoryId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByObservatoryId(@PathVariable id: UUID): Iterable<UserController.UserDetails> {
        return userService.findUsersByObservatoryId(id)
            .map(UserController::UserDetails)
    }

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryName(@PathVariable id: UUID, @RequestBody name: String): Observatory {
        var response = service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }
        println(name)
        response.name = name.replace("\"", "")

        return service.updateObservatory(response)
    }

    @RequestMapping("{id}/description", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryDescription(@PathVariable id: UUID, @RequestBody description: String): Observatory {
        var response = service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }
        response.description = description.replace("\"", "")

        return service.updateObservatory(response)
    }

    @RequestMapping("{id}/researchField", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryResearchField(@PathVariable id: UUID, @RequestBody researchField: String): Observatory {
        var response = service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }
        response.researchField = researchField.replace("\"", "")

        return service.updateObservatory(response)
    }

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: UUID,
        val description: String,
        val researchField: String
    )

    data class ErrorMessage(
        val message: String
    )
}
