package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Size
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
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/api/observatories/")
@CrossOrigin(origins = ["*"])
class ObservatoryController(
    private val service: ObservatoryService,
    private val userService: UserService,
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService,
    private val neo4jStatsService: Neo4jStatsService
) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (service.findByName(observatory.observatoryName).isEmpty) {
            val organizationEntity = organizationService.findById(observatory.organizationId)
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
    fun updateObservatoryName(@PathVariable id: UUID, @RequestBody @Valid name: UpdateRequest): Observatory {
        val response: Observatory = service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }

        response.name = name.value

        return service.updateObservatory(response)
    }

    @RequestMapping("{id}/description", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryDescription(@PathVariable id: UUID, @RequestBody @Valid description: UpdateRequest): Observatory {
        val response = service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }
        response.description = description.value

        return service.updateObservatory(response)
    }

    @RequestMapping("{id}/research_field", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryResearchField(@PathVariable id: UUID, @RequestBody @Valid researchField: UpdateRequest): Observatory {
        val response = service
            .findById(id)
            .orElseThrow { ObservatoryNotFound() }
        response.researchField = researchField.value

        return service.updateObservatory(response)
    }

    @GetMapping("stats/observatories")
    fun findObservatoriesWithStats(): List<Observatory> {
        val totalObservatories = service.listObservatories()
        val totalPapers = (neo4jStatsService.getObservatoriesPapersAndComparisonsCount()).associateBy { it.observatoryId }
        totalObservatories.forEach {
            if (it.id.toString() in totalPapers)
                it.numPapers = totalPapers[it.id.toString()]?.resources ?: 0
                it.numComparisons = totalPapers[it.id.toString()]?.comparisons ?: 0
        }

        return totalObservatories
    }

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: UUID,
        val description: String,
        val researchField: String
    )

    data class UpdateRequest(
        @field:NotBlank
        @field:Size(min = 1)
        val value: String
    )

    data class ErrorMessage(
        val message: String
    )
}
