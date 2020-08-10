package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
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
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories/")
@CrossOrigin(origins = ["*"])
class ObservatoryController(
    private val service: ObservatoryService,
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService,
    private val contributorService: ContributorService
) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (service.findByName(observatory.observatoryName).isEmpty) {
            var organizationEntity = organizationService.findById(observatory.organizationId)
            val id = service.create(observatory.observatoryName, observatory.description, organizationEntity.get()).id
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
            .orElseThrow { ObservatoryNotFound(id) }

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
    fun findUsersByObservatoryId(@PathVariable id: UUID): Iterable<Contributor> =
        contributorService.findUsersByObservatoryId(id)

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: UUID,
        val description: String
    )

    data class ErrorMessage(
        val message: String
    )
}
