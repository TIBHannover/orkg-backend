package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.RosettaStoneTemplateRepresentationAdapter
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.input.CreateRosettaStoneTemplateUseCase
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.UpdateRosettaStoneTemplateUseCase
import org.orkg.graph.domain.DynamicLabel
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

const val ROSETTA_STONE_TEMPLATE_JSON_V1 = "application/vnd.orkg.rosetta-stone-template.v1+json"

@RestController
@RequestMapping("/api/rosetta-stone/templates", produces = [ROSETTA_STONE_TEMPLATE_JSON_V1])
class RosettaStoneTemplateController(
    private val service: RosettaStoneTemplateUseCases,
) : RosettaStoneTemplateRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
    ): RosettaStoneTemplateRepresentation =
        service.findById(id)
            .mapToRosettaStoneTemplateRepresentation()
            .orElseThrow { RosettaStoneTemplateNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable,
    ): Page<RosettaStoneTemplateRepresentation> =
        service.findAll(
            searchString = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            pageable = pageable
        ).mapToRosettaStoneTemplateRepresentation()

    @RequireLogin
    @PostMapping(consumes = [ROSETTA_STONE_TEMPLATE_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateRosettaStoneTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/rosetta-stone/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [ROSETTA_STONE_TEMPLATE_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateRosettaStoneTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/rosetta-stone/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: ThingId,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.delete(id, userId)
        return noContent().build()
    }

    data class CreateRosettaStoneTemplateRequest(
        @field:NotBlank
        val label: String,
        @field:NotBlank
        val description: String,
        @field:NotBlank
        @JsonProperty("formatted_label")
        val dynamicLabel: String,
        @field:NotBlank
        @JsonProperty("example_usage")
        val exampleUsage: String,
        @field:Valid
        @field:Size(min = 1)
        val properties: List<TemplatePropertyRequest>,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateRosettaStoneTemplateUseCase.CreateCommand =
            CreateRosettaStoneTemplateUseCase.CreateCommand(
                contributorId = contributorId,
                label = label,
                description = description,
                dynamicLabel = DynamicLabel(dynamicLabel),
                exampleUsage = exampleUsage,
                properties = properties.map { it.toTemplatePropertyCommand() },
                observatories = observatories,
                organizations = organizations
            )
    }

    data class UpdateRosettaStoneTemplateRequest(
        @field:NotBlank
        val label: String?,
        @field:NullableNotBlank
        val description: String?,
        @field:NullableNotBlank
        @JsonProperty("formatted_label")
        val dynamicLabel: String?,
        @field:NotBlank
        @JsonProperty("example_usage")
        val exampleUsage: String?,
        @field:Valid
        @field:Size(min = 1)
        val properties: List<TemplatePropertyRequest>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
    ) {
        fun toUpdateCommand(templateId: ThingId, contributorId: ContributorId): UpdateRosettaStoneTemplateUseCase.UpdateCommand =
            UpdateRosettaStoneTemplateUseCase.UpdateCommand(
                templateId = templateId,
                contributorId = contributorId,
                label = label,
                description = description,
                dynamicLabel = dynamicLabel?.let(::DynamicLabel),
                exampleUsage = exampleUsage,
                properties = properties?.map { it.toTemplatePropertyCommand() },
                observatories = observatories,
                organizations = organizations
            )
    }
}
