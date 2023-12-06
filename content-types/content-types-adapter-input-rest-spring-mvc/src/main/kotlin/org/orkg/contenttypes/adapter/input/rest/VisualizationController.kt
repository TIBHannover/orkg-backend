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
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.contenttypes.adapter.input.rest.mapping.VisualizationRepresentationAdapter
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.CreateVisualizationUseCase
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

const val VISUALIZATION_JSON_V2 = "application/vnd.orkg.visualization.v2+json"

@RestController
@RequestMapping("/api/visualizations", produces = [MediaType.APPLICATION_JSON_VALUE])
class VisualizationController(
    private val service: VisualizationUseCases
) : BaseController(), VisualizationRepresentationAdapter {

    @GetMapping("/{id}", produces = [VISUALIZATION_JSON_V2])
    fun findById(
        @PathVariable id: ThingId
    ): VisualizationRepresentation =
        service.findById(id)
            .mapToVisualizationRepresentation()
            .orElseThrow { VisualizationNotFound(id) }

    @GetMapping(produces = [VISUALIZATION_JSON_V2])
    fun findAll(
        @RequestParam("title", required = false) title: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<VisualizationRepresentation> {
        if (setOf(title, visibility, createdBy).size > 2)
            throw TooManyParameters.atMostOneOf("title", "visibility", "created_by")
        return when {
            title != null -> service.findAllByTitle(title, pageable)
            visibility != null -> service.findAllByVisibility(visibility, pageable)
            createdBy != null -> service.findAllByContributor(createdBy, pageable)
            else -> service.findAll(pageable)
        }.mapToVisualizationRepresentation()
    }

    @GetMapping(params = ["visibility", "research_field"], produces = [VISUALIZATION_JSON_V2])
    fun findAll(
        @RequestParam("visibility") visibility: VisibilityFilter,
        @RequestParam("research_field") researchField: ThingId,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<VisualizationRepresentation> =
        service.findAllByResearchFieldAndVisibility(researchField, visibility, includeSubfields, pageable)
            .mapToVisualizationRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [VISUALIZATION_JSON_V2], produces = [VISUALIZATION_JSON_V2])
    fun create(
        @RequestBody @Valid request: CreateVisualizationRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/visualizations/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateVisualizationRequest(
        @field:NotBlank
        val title: String,
        @field:NotBlank
        val description: String,
        @field:Valid
        val authors: List<AuthorDTO>,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateVisualizationUseCase.CreateCommand =
            CreateVisualizationUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                description = description,
                authors = authors.map { it.toCreateCommand() },
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod
            )
    }
}
