package org.orkg.community.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.community.adapter.input.rest.mapping.ObservatoryRepresentationAdapter
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.ObservatoryURLNotFound
import org.orkg.community.input.CreateObservatoryUseCase.CreateCommand
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.UpdateObservatoryUseCase.UpdateCommand
import org.orkg.community.output.ObservatoryRepository
import org.orkg.graph.input.ResourceUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/observatories", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObservatoryController(
    private val service: ObservatoryUseCases,
    override val resourceRepository: ResourceUseCases,
    private val observatoryRepository: ObservatoryRepository,
) : ObservatoryRepresentationAdapter {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun create(
        @RequestBody @Valid request: CreateObservatoryRequest,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<ObservatoryRepresentation> {
        val id = service.create(request.toCreateCommand())
        val location = uriComponentsBuilder
            .path("/api/observatories/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).body(service.findById(id).mapToObservatoryRepresentation().get())
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: String,
    ): ObservatoryRepresentation = if (isValidUUID(id)) {
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

    @GetMapping
    fun findAll(
        @RequestParam(value = "q", required = false) name: String?,
        @RequestParam(value = "research_field", required = false) researchField: ThingId?,
        pageable: Pageable,
    ): Page<ObservatoryRepresentation> {
        if (name != null && researchField != null) {
            throw TooManyParameters.atMostOneOf("q", "research_field")
        }
        return when {
            name != null -> service.findAllByNameContains(name, pageable)
            researchField != null -> service.findAllByResearchFieldId(researchField, pageable)
            else -> service.findAll(pageable)
        }.mapToObservatoryRepresentation()
    }

    @GetMapping("/research-fields")
    fun findAllResearchFields(
        pageable: Pageable,
    ): Page<ResearchFieldRepresentation> =
        service.findAllResearchFields(pageable).mapToResearchFieldRepresentation()

    @GetMapping("/{id}/users")
    fun findAllUsersByObservatoryId(
        @PathVariable id: ObservatoryId,
        pageable: Pageable,
    ): Page<Contributor> =
        observatoryRepository.findAllMembersByObservatoryId(id, pageable)

    @RequireCuratorRole
    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid request: UpdateObservatoryRequest,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Any> {
        service.update(request.toUpdateCommand(id))
        val location = uriComponentsBuilder.path("/api/observatories/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequestMapping("/{id}/name", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateObservatoryName(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid name: UpdateRequest,
    ): ObservatoryRepresentation {
        service.changeName(id, name.value)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("/{id}/description", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateObservatoryDescription(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid description: UpdateRequest,
    ): ObservatoryRepresentation {
        service.changeDescription(id, description.value)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("/{id}/research_field", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun updateObservatoryResearchField(
        @PathVariable id: ObservatoryId,
        @RequestBody @Valid request: UpdateRequest,
    ): ObservatoryRepresentation {
        service.changeResearchField(id, ThingId(request.value))
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("/add/{id}/organization", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun addObservatoryOrganization(
        @PathVariable id: ObservatoryId,
        @RequestBody organizationRequest: UpdateOrganizationRequest,
    ): ObservatoryRepresentation {
        service.addOrganization(id, organizationRequest.organizationId)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    @RequestMapping("/delete/{id}/organization", method = [RequestMethod.POST, RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun deleteObservatoryOrganization(
        @PathVariable id: ObservatoryId,
        @RequestBody organizationRequest: UpdateOrganizationRequest,
    ): ObservatoryRepresentation {
        service.deleteOrganization(id, organizationRequest.organizationId)
        return service.findById(id).mapToObservatoryRepresentation().get()
    }

    fun isValidUUID(id: String): Boolean = try {
        UUID.fromString(id) != null
    } catch (e: IllegalArgumentException) {
        false
    }

    data class CreateObservatoryRequest(
        @JsonAlias("observatory_name")
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
        val displayId: String,
        @field:Valid
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
    ) {
        fun toCreateCommand() = CreateCommand(
            name = name,
            description = description,
            organizations = setOf(organizationId),
            researchField = researchField,
            displayId = displayId,
            sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty()
        )
    }

    data class UpdateObservatoryRequest(
        @field:Valid
        @field:Size(min = 1)
        val name: String?,
        @field:Valid
        val organizations: Set<OrganizationId>?,
        val description: String?,
        @JsonProperty("research_field")
        val researchField: ThingId?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
    ) {
        fun toUpdateCommand(id: ObservatoryId) = UpdateCommand(
            id = id,
            name = name,
            organizations = organizations,
            description = description,
            researchField = researchField,
            sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty()
        )
    }

    data class UpdateRequest(
        @field:NotBlank
        val value: String,
    )

    data class UpdateOrganizationRequest(
        @JsonProperty("organization_id")
        val organizationId: OrganizationId,
    )
}
