package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
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

    @GetMapping("/research_field")
    fun findObservatoriesByResearchField(): Map<String?, List<Observatory>> {
        var list = service.listObservatories()

        list.forEach {
            if (it.researchField?.id === null)
                it.researchField?.label = "Others"
        }
        return list.groupBy { it.researchField?.label }
    }

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryName(@PathVariable id: UUID, @RequestBody @Valid name: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeName(id, name.value)
    }

    @RequestMapping("{id}/description", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryDescription(@PathVariable id: UUID, @RequestBody @Valid description: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeDescription(id, description.value)
    }

    @RequestMapping("{id}/research_field", method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateObservatoryResearchField(@PathVariable id: UUID, @RequestBody @Valid researchFieldId: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeResearchField(id, researchFieldId.value)
    }

    @GetMapping("stats/observatories")
    fun findObservatoriesWithStats(): List<ObservatoryResources> {
        return neo4jStatsService.getObservatoriesPapersAndComparisonsCount()
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
