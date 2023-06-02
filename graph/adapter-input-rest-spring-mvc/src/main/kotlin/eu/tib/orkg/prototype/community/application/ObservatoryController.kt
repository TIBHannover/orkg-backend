package eu.tib.orkg.prototype.community.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.ObservatoryRepresentation
import eu.tib.orkg.prototype.community.ObservatoryRepresentationAdapter
import eu.tib.orkg.prototype.community.api.CreateObservatoryUseCase.*
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObservatoryController(
    private val service: ObservatoryUseCases,
    private val contributorService: ContributorService,
    override val resourceRepository: ResourceUseCases
) : ObservatoryRepresentationAdapter {
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun create(
        @RequestBody @Valid request: CreateObservatoryRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ObservatoryRepresentation> {
        if (service.findByName(request.name).isPresent) {
            throw ObservatoryAlreadyExists.withName(request.name)
        } else if (service.findByDisplayId(request.displayId).isPresent) {
            throw ObservatoryAlreadyExists.withDisplayId(request.displayId)
        }
        val id = service.create(request.toCreateCommand())
        val location = uriComponentsBuilder
            .path("api/observatories/{id}")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(service.findById(id).mapToObservatoryRepresentation().get())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ObservatoryRepresentation {
        return if (isValidUUID(id)) {
            service
                .findById(ObservatoryId(UUID.fromString(id)))
                .mapToObservatoryRepresentation()
                .orElseThrow { ObservatoryNotFound(ObservatoryId(UUID.fromString(id))) }
        } else {
            service
                .findByDisplayId(id)
                .mapToObservatoryRepresentation()
                .orElseThrow { ObservatoryURLNotFound(id) }
        }
    }

    @GetMapping("/")
    fun findAll(
        @RequestParam(value = "research_field", required = false) researchField: ThingId?,
        pageable: Pageable
    ): Page<ObservatoryRepresentation> =
        when (researchField) {
            null -> service.findAll(pageable)
            else -> service.findAllByResearchField(researchField, pageable)
        }.mapToObservatoryRepresentation()

    @GetMapping("{id}/users")
    fun findAllUsersByObservatoryId(
        @PathVariable id: ObservatoryId,
        pageable: Pageable
    ): Page<Contributor> =
        contributorService.findAllByObservatoryId(id, pageable)

    @RequestMapping("{id}/name", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateObservatoryName(@PathVariable id: ObservatoryId, @RequestBody @Valid name: UpdateRequest): ObservatoryRepresentation {
        service.changeName(id, name.value)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("{id}/description", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateObservatoryDescription(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid description: UpdateRequest
    ): ObservatoryRepresentation {
        service.changeDescription(id, description.value)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("{id}/research_field", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateObservatoryResearchField(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid request: UpdateRequest
    ): ObservatoryRepresentation {
        service.changeResearchField(id, ThingId(request.value))
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("add/{id}/organization", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun addObservatoryOrganization(
        @PathVariable id: ObservatoryId,
        @RequestBody organizationRequest: UpdateOrganizationRequest
    ): ObservatoryRepresentation {
        service.addOrganization(id, organizationRequest.organizationId)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("delete/{id}/organization", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteObservatoryOrganization(
        @PathVariable id: ObservatoryId,
        @RequestBody organizationRequest: UpdateOrganizationRequest
    ): ObservatoryRepresentation {
        service.deleteOrganization(id, organizationRequest.organizationId)
        return service.findById(id).mapToObservatoryRepresentation().get()
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
        val name: String,
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
    ) {
        fun toCreateCommand() = CreateCommand(
            name = name,
            description = description,
            organizationId = organizationId,
            researchField = researchField,
            displayId = displayId
        )
    }

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
