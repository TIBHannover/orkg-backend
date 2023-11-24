package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRelatedFigureRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRelatedResourceRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRepresentationAdapter
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

const val COMPARISON_JSON_V2 = "application/vnd.orkg.comparison.v2+json"

@RestController
@RequestMapping("/api/comparisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class ComparisonController(
    private val service: ComparisonUseCases
) : BaseController(), ComparisonRepresentationAdapter, ComparisonRelatedResourceRepresentationAdapter,
    ComparisonRelatedFigureRepresentationAdapter {

    @GetMapping("/{id}", produces = [COMPARISON_JSON_V2])
    fun findById(
        @PathVariable id: ThingId
    ): ComparisonRepresentation =
        service.findById(id)
            .mapToComparisonRepresentation()
            .orElseThrow { ComparisonNotFound(id) }

    @GetMapping(produces = [COMPARISON_JSON_V2])
    fun findAll(
        @RequestParam("doi", required = false) doi: String?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<ComparisonRepresentation> {
        if (setOf(doi, title, visibility, createdBy).size > 2)
            throw TooManyParameters.atMostOneOf("doi", "title", "visibility", "created_by")
        return when {
            doi != null -> service.findAllByDOI(doi, pageable)
            title != null -> service.findAllByTitle(title, pageable)
            visibility != null -> service.findAllByVisibility(visibility, pageable)
            createdBy != null -> service.findAllByContributor(createdBy, pageable)
            else -> service.findAll(pageable)
        }.mapToComparisonRepresentation()
    }

    @GetMapping(params = ["visibility", "research_field"], produces = [COMPARISON_JSON_V2])
    fun findAll(
        @RequestParam("visibility") visibility: VisibilityFilter,
        @RequestParam("research_field") researchField: ThingId,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<ComparisonRepresentation> =
        service.findAllByResearchFieldAndVisibility(researchField, visibility, includeSubfields, pageable)
            .mapToComparisonRepresentation()

    @PostMapping(consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @RequestBody @Validated request: CreateComparisonRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @GetMapping("/{id}/related-resources/{resourceId}", produces = [COMPARISON_JSON_V2])
    fun findRelatedResourceById(
        @PathVariable("id") id: ThingId,
        @PathVariable("resourceId") resourceId: ThingId
    ): ComparisonRelatedResourceRepresentation =
        service.findRelatedResourceById(id, resourceId)
            .mapToComparisonRelatedResourceRepresentation()
            .orElseThrow { ComparisonRelatedResourceNotFound(resourceId) }

    @GetMapping("/{id}/related-resources", produces = [COMPARISON_JSON_V2])
    fun findAllRelatedResources(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ComparisonRelatedResourceRepresentation> =
        service.findAllRelatedResources(id, pageable)
            .mapToComparisonRelatedResourceRepresentation()

    @PostMapping("/{comparisonId}/related-resources", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @PathVariable("comparisonId") comparisonId: ThingId,
        @RequestBody @Validated request: CreateComparisonRelatedResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.createComparisonRelatedResource(request.toCreateCommand(comparisonId, userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{comparisonId}/related-resources/{id}")
            .buildAndExpand(comparisonId, id)
            .toUri()
        return noContent().location(location).build()
    }

    @GetMapping("/{id}/related-figures/{figureId}", produces = [COMPARISON_JSON_V2])
    fun findRelatedFigureById(
        @PathVariable("id") id: ThingId,
        @PathVariable("figureId") figureId: ThingId
    ): ComparisonRelatedFigureRepresentation =
        service.findRelatedFigureById(id, figureId)
            .mapToComparisonRelatedFigureRepresentation()
            .orElseThrow { ComparisonRelatedFigureNotFound(figureId) }

    @GetMapping("/{id}/related-figures", produces = [COMPARISON_JSON_V2])
    fun findAllRelatedFigures(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ComparisonRelatedFigureRepresentation> =
        service.findAllRelatedFigures(id, pageable)
            .mapToComparisonRelatedFigureRepresentation()

    @PostMapping("/{comparisonId}/related-figures", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @PathVariable("comparisonId") comparisonId: ThingId,
        @RequestBody @Validated request: CreateComparisonRelatedFigureRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.createComparisonRelatedFigure(request.toCreateCommand(comparisonId, userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{comparisonId}/related-figures/{id}")
            .buildAndExpand(comparisonId, id)
            .toUri()
        return noContent().location(location).build()
    }

    @PostMapping("/{id}/publish", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        service.publish(id, request.subject, request.description)
        val location = uriComponentsBuilder
            .path("api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateComparisonRequest(
        @NotBlank
        val title: String,
        @NotBlank
        val description: String,
        @Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        val authors: List<AuthorDTO>,
        val contributions: List<ThingId>,
        val references: List<String>,
        @Size(max = 1)
        val observatories: List<ObservatoryId>,
        @Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("is_anonymized")
        val isAnonymized: Boolean,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateComparisonUseCase.CreateCommand =
            CreateComparisonUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                description = description,
                researchFields = researchFields,
                authors = authors.map { it.toCreateCommand() },
                contributions = contributions,
                references = references,
                observatories = observatories,
                organizations = organizations,
                isAnonymized = isAnonymized,
                extractionMethod = extractionMethod
            )
    }

    data class CreateComparisonRelatedResourceRequest(
        val label: String,
        @NotBlank
        val image: String?,
        @NotBlank
        val url: String?,
        @NotBlank
        val description: String?
    ) {
        fun toCreateCommand(comparisonId: ThingId, contributorId: ContributorId) =
            CreateComparisonUseCase.CreateComparisonRelatedResourceCommand(
                comparisonId = comparisonId,
                contributorId = contributorId,
                label = label,
                image = image,
                url = url,
                description = description
            )
    }

    data class CreateComparisonRelatedFigureRequest(
        val label: String,
        @NotBlank
        val image: String?,
        @NotBlank
        val description: String?
    ) {
        fun toCreateCommand(comparisonId: ThingId, contributorId: ContributorId) =
            CreateComparisonUseCase.CreateComparisonRelatedFigureCommand(
                comparisonId = comparisonId,
                contributorId = contributorId,
                label = label,
                image = image,
                description = description
            )
    }

    data class PublishRequest(
        @NotBlank
        val subject: String,
        @NotBlank
        val description: String
    )
}
