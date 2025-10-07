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
import org.orkg.contenttypes.adapter.input.rest.mapping.VisualizationRepresentationAdapter
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.CreateVisualizationUseCase
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

const val VISUALIZATION_JSON_V2 = "application/vnd.orkg.visualization.v2+json"

@RestController
@RequestMapping("/api/visualizations", produces = [MediaType.APPLICATION_JSON_VALUE])
class VisualizationController(
    private val service: VisualizationUseCases,
) : VisualizationRepresentationAdapter {
    @GetMapping("/{id}", produces = [VISUALIZATION_JSON_V2])
    fun findById(
        @PathVariable id: ThingId,
    ): VisualizationRepresentation =
        service.findById(id)
            .mapToVisualizationRepresentation()
            .orElseThrow { VisualizationNotFound(id) }

    @GetMapping(produces = [VISUALIZATION_JSON_V2])
    fun findAll(
        @RequestParam("title", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("research_problem", required = false) researchProblem: ThingId?,
        pageable: Pageable,
    ): Page<VisualizationRepresentation> =
        service.findAll(
            label = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            researchProblem = researchProblem,
            pageable = pageable
        ).mapToVisualizationRepresentation()

    @RequireLogin
    @PostMapping(consumes = [VISUALIZATION_JSON_V2], produces = [VISUALIZATION_JSON_V2])
    fun create(
        @RequestBody @Valid request: CreateVisualizationRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/visualizations/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    data class CreateVisualizationRequest(
        @field:NotBlank
        val title: String,
        @field:NotBlank
        val description: String,
        @field:Valid
        val authors: List<AuthorRequest>,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateVisualizationUseCase.CreateCommand =
            CreateVisualizationUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                description = description,
                authors = authors.map { it.toAuthor() },
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod
            )
    }
}
