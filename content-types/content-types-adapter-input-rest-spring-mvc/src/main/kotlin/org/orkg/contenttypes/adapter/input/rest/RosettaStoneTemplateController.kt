package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.mapping.RosettaStoneTemplateRepresentationAdapter
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.input.CreateRosettaStoneTemplateUseCase
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

const val ROSETTA_STONE_TEMPLATE_JSON_V1 = "application/vnd.orkg.rosetta-stone-template.v1+json"

@RestController
@RequestMapping("/api/rosetta-stone/templates", produces = [ROSETTA_STONE_TEMPLATE_JSON_V1])
class RosettaStoneTemplateController(
    private val service: RosettaStoneTemplateUseCases
) : RosettaStoneTemplateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
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
        pageable: Pageable
    ): Page<RosettaStoneTemplateRepresentation> =
        service.findAll(
            searchString = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            pageable = pageable
        ).mapToRosettaStoneTemplateRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [ROSETTA_STONE_TEMPLATE_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateRosettaStoneTemplateRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/rosetta-stone/templates/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    data class CreateRosettaStoneTemplateRequest(
        @field:NotBlank
        val label: String,
        @field:NotBlank
        val description: String,
        @field:NotBlank
        @JsonProperty("formatted_label")
        val formattedLabel: String,
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
                formattedLabel = FormattedLabel.of(formattedLabel),
                exampleUsage = exampleUsage,
                properties = properties.map { it.toTemplatePropertyDefinition() },
                observatories = observatories,
                organizations = organizations
            )
    }
}
