package eu.tib.orkg.prototype.community.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.StatisticsService
import eu.tib.orkg.prototype.statements.spi.ObservatoryResources
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObservatoryController(
    private val service: ObservatoryUseCases,
    private val resourceService: ResourceUseCases,
    private val contributorService: ContributorService,
    private val statisticsService: StatisticsService
) {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun addObservatory(
        @RequestBody @Valid observatory: CreateObservatoryRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        if (service.findByName(observatory.observatoryName).isPresent) {
            throw ObservatoryAlreadyExists.withName(observatory.observatoryName)
        } else if (service.findByDisplayId(observatory.displayId).isPresent) {
            throw ObservatoryAlreadyExists.withDisplayId(observatory.displayId)
        }
        val id = service.create(
            id = null,
            observatory.observatoryName,
            observatory.description,
            observatory.organizationId,
            observatory.researchField,
            observatory.displayId
        )
        val location = uriComponentsBuilder
            .path("api/observatories/{id}")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(service.findById(id).get())
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
        return service.listObservatories(PageRequest.of(0, Int.MAX_VALUE)).content
    }

    @GetMapping("{id}/users")
    fun findUsersByObservatoryId(@PathVariable id: ObservatoryId): Iterable<Contributor> =
        contributorService.findUsersByObservatoryId(id)

    @GetMapping("research-field/{id}/observatories")
    fun findObservatoriesByResearchField(
        @PathVariable id: ThingId
    ): List<Observatory>? {
        resourceService.findById(id).orElseThrow { ResearchFieldNotFound(id) }
        return service.findObservatoriesByResearchField(id, PageRequest.of(0, Int.MAX_VALUE)).content
    }

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateObservatoryName(@PathVariable id: ObservatoryId, @RequestBody @Valid name: UpdateRequest): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeName(id, name.value)
    }

    @RequestMapping("{id}/description", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateObservatoryDescription(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid description: UpdateRequest
    ): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.changeDescription(id, description.value)
    }

    @RequestMapping("{id}/research_field", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateObservatoryResearchField(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid request: UpdateRequest
    ): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
        val researchField = resourceService.findById(ThingId(request.value))
            .orElseThrow { ResearchFieldNotFound(ThingId(request.value)) }
        return service.changeResearchField(id, ResearchField(researchField.id.value, researchField.label))
    }

    @RequestMapping("add/{id}/organization", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun addObservatoryOrganization(
        @PathVariable id: ObservatoryId,
        @RequestBody organizationRequest: UpdateOrganizationRequest
    ): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.addOrganization(id, organizationRequest.organizationId)
    }

    @RequestMapping("delete/{id}/organization", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteObservatoryOrganization(
        @PathVariable id: ObservatoryId,
        @RequestBody organizationRequest: UpdateOrganizationRequest
    ): Observatory {
        service
            .findById(id)
            .orElseThrow { ObservatoryNotFound(id) }

        return service.deleteOrganization(id, organizationRequest.organizationId)
    }

    @GetMapping("stats/observatories")
    fun findObservatoriesWithStats(): List<ObservatoryResources> {
        return statisticsService.getObservatoriesPapersAndComparisonsCount()
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
        val researchField: ThingId,
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

    data class UpdateOrganizationRequest(
        @JsonProperty("organization_id")
        val organizationId: OrganizationId
    )
}
