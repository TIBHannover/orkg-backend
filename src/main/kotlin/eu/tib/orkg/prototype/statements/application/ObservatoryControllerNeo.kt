package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryServiceNeo
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatoriesneo/")
class ObservatoryControllerNeo(
    private val service: ObservatoryServiceNeo,
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService,
    private val contributorService: ContributorService,
    private val neo4jStatsService: Neo4jStatsService
) {

    @PostMapping("/")
    fun addObservatory(@RequestBody observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (service.findByName(observatory.observatoryName).isEmpty) {
            val id = service.create(observatory.observatoryName, observatory.description, observatory.organizationId, observatory.researchField).id
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
    fun findById(@PathVariable id: ObservatoryId): Observatory =
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

    @GetMapping("/")
    fun findObservatories(): List<Observatory> {
        return service.listObservatories()
    }

    data class CreateObservatoryRequest(
        val observatoryName: String,
        val organizationId: OrganizationId,
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
