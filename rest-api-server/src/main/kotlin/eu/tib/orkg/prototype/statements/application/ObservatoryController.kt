package eu.tib.orkg.prototype.statements.application
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import org.springframework.http.ResponseEntity
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
class ObservatoryController(
    private val service: ObservatoryService,
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService,
    private val contributorService: ContributorService,
    private val neo4jStatsService: Neo4jStatsService
) {

    @PostMapping("/")
    fun addObservatory(@RequestBody @Valid observatory: CreateObservatoryRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        return if (service.findByName(observatory.observatoryName).isEmpty && service.findByDisplayId(observatory.displayId).isEmpty) {
            val organizationEntity = organizationService.findById(observatory.organizationId)
            val id = service.create(
                observatory.observatoryName,
                observatory.description,
                organizationEntity.get(),
                observatory.researchField,
                observatory.displayId
            ).id
            val location = uriComponentsBuilder
                .path("api/observatories/{id}")
                .buildAndExpand(id)
                .toUri()
            ResponseEntity.created(location).body(service.findById(id!!).get())
        } else
            ResponseEntity.badRequest().body(
                    ErrorMessage(message = "Observatory with same name or URL already exist")
                )
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): Observatory {
        return if (isValidUUID(id)) {
            service
                .findById(ObservatoryId(UUID.fromString(id)))
                .orElseThrow { ObservatoryNotFound(ObservatoryId(UUID.fromString(id))) }
        } else {
            service
                .findByDisplayId(id)
                .orElseThrow { ObservatoryURLNotFound(id) }
        }
    }

    @GetMapping("/")
    fun findObservatories(): List<Observatory> {
        return service.listObservatories()
    }

    @GetMapping("{id}/papers")
    fun findPapersByObservatoryId(@PathVariable id: ObservatoryId): Iterable<Resource> {
        return resourceService.findPapersByObservatoryId(id)
    }

    @GetMapping("{id}/comparisons")
    fun findComparisonsByObservatoryId(@PathVariable id: ObservatoryId): Iterable<Resource> {
        return resourceService.findComparisonsByObservatoryId(id)
    }

    @GetMapping("{id}/problems")
    fun findProblemsByObservatoryId(@PathVariable id: ObservatoryId): Iterable<Resource> {
        return resourceService.findProblemsByObservatoryId(id)
    }

    @GetMapping("{id}/users")
    fun findUsersByObservatoryId(@PathVariable id: ObservatoryId): Iterable<Contributor> =
        contributorService.findUsersByObservatoryId(id)

    @GetMapping("research-field/{id}/observatories")
    fun findObservatoriesByResearchField(
        @PathVariable id: String
    ): List<Observatory>? {
        return service.findObservatoriesByResearchField(id)
    }

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryName(@PathVariable id: ObservatoryId, @RequestBody @Valid name: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeName(id, name.value)
    }

    @RequestMapping("{id}/description", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryDescription(@PathVariable id: ObservatoryId, @RequestBody @Valid description: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeDescription(id, description.value)
    }

    @RequestMapping("{id}/research_field", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryResearchField(@PathVariable id: ObservatoryId, @RequestBody @Valid researchFieldId: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeResearchField(id, researchFieldId.value)
    }

    @GetMapping("stats/observatories")
    fun findObservatoriesWithStats(): List<ObservatoryResources> {
        return neo4jStatsService.getObservatoriesPapersAndComparisonsCount()
    }

    fun isValidUUID(id: String): Boolean {
        return try {
            UUID.fromString(id) != null
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    data class CreateObservatoryRequest(
        @JsonProperty("observatory_name")
        val observatoryName: String,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId,
        val description: String,
        @JsonProperty("research_field")
        val researchField: String,
        @field:Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Only underscores ( _ ), numbers, and letters are allowed in the permalink field"
        )
        @field:NotBlank
        @JsonProperty("display_id")
        val displayId: String
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
