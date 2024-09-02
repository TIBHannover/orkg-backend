package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRelatedFigureRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRelatedResourceRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRepresentationAdapter
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.PublishComparisonUseCase
import org.orkg.contenttypes.input.UpdateComparisonUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
) : ComparisonRepresentationAdapter, ComparisonRelatedResourceRepresentationAdapter,
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
        @RequestParam("title", required = false) title: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("doi", required = false) doi: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("verified", required = false) verified: Boolean?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("sdg", required = false) sustainableDevelopmentGoal: ThingId?,
        pageable: Pageable
    ): Page<ComparisonRepresentation> =
        service.findAll(
            pageable = pageable,
            doi = doi,
            label = title?.let { SearchString.of(title, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).mapToComparisonRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @RequestBody @Valid request: CreateComparisonRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateComparisonRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
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

    @PreAuthorizeUser
    @PostMapping("/{comparisonId}/related-resources", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @PathVariable("comparisonId") comparisonId: ThingId,
        @RequestBody @Valid request: CreateComparisonRelatedResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.createComparisonRelatedResource(request.toCreateCommand(comparisonId, userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{comparisonId}/related-resources/{id}")
            .buildAndExpand(comparisonId, id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{comparisonId}/related-resources/{comparisonRelatedResourceId}", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun updateRelatedResource(
        @PathVariable("comparisonId") comparisonId: ThingId,
        @PathVariable("comparisonRelatedResourceId") comparisonRelatedResourceId: ThingId,
        @RequestBody @Valid request: UpdateComparisonRelatedResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.updateComparisonRelatedResource(request.toUpdateCommand(comparisonId, comparisonRelatedResourceId, userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{comparisonId}/related-resources/{id}")
            .buildAndExpand(comparisonId, comparisonRelatedResourceId)
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

    @PreAuthorizeUser
    @PostMapping("/{comparisonId}/related-figures", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @PathVariable("comparisonId") comparisonId: ThingId,
        @RequestBody @Valid request: CreateComparisonRelatedFigureRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.createComparisonRelatedFigure(request.toCreateCommand(comparisonId, userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{comparisonId}/related-figures/{id}")
            .buildAndExpand(comparisonId, id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{comparisonId}/related-figures/{comparisonRelatedFigureId}", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun updateRelatedFigure(
        @PathVariable("comparisonId") comparisonId: ThingId,
        @PathVariable("comparisonRelatedFigureId") comparisonRelatedFigureId: ThingId,
        @RequestBody @Valid request: UpdateComparisonRelatedFigureRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.updateComparisonRelatedFigure(request.toUpdateCommand(comparisonId, comparisonRelatedFigureId, userId))
        val location = uriComponentsBuilder
            .path("api/comparisons/{comparisonId}/related-figures/{id}")
            .buildAndExpand(comparisonId, comparisonRelatedFigureId)
            .toUri()
        return noContent().location(location).build()
    }

    @PreAuthorizeUser
    @PostMapping("/{id}/publish", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        service.publish(request.toPublishCommand(id, contributorId))
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
        @field:Valid
        val authors: List<AuthorDTO>,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
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
                authors = authors.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty(),
                contributions = contributions,
                references = references,
                observatories = observatories,
                organizations = organizations,
                isAnonymized = isAnonymized,
                extractionMethod = extractionMethod
            )
    }

    data class UpdateComparisonRequest(
        @NotBlank
        val title: String?,
        @NotBlank
        val description: String?,
        @Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>?,
        @field:Valid
        val authors: List<AuthorDTO>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val contributions: List<ThingId>?,
        val references: List<String>?,
        @Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("is_anonymized")
        val isAnonymized: Boolean?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?
    ) {
        fun toUpdateCommand(comparisonId: ThingId, contributorId: ContributorId): UpdateComparisonUseCase.UpdateCommand =
            UpdateComparisonUseCase.UpdateCommand(
                comparisonId = comparisonId,
                contributorId = contributorId,
                title = title,
                description = description,
                researchFields = researchFields,
                authors = authors?.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
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
        @field:NullableNotBlank
        val image: String?,
        @field:NullableNotBlank
        val url: String?,
        @field:NullableNotBlank
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

    data class UpdateComparisonRelatedResourceRequest(
        val label: String?,
        @field:NullableNotBlank
        val image: String?,
        @field:NullableNotBlank
        val url: String?,
        @field:NullableNotBlank
        val description: String?
    ) {
        fun toUpdateCommand(comparisonId: ThingId, comparisonRelatedResourceId: ThingId, contributorId: ContributorId) =
            UpdateComparisonUseCase.UpdateComparisonRelatedResourceCommand(
                comparisonId, comparisonRelatedResourceId, contributorId, label, image, url, description
            )
    }

    data class CreateComparisonRelatedFigureRequest(
        val label: String,
        @field:NullableNotBlank
        val image: String?,
        @field:NullableNotBlank
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

    data class UpdateComparisonRelatedFigureRequest(
        val label: String?,
        @field:NotBlank
        val image: String,
        @field:NotBlank
        val description: String
    ) {
        fun toUpdateCommand(comparisonId: ThingId, comparisonRelatedFigureId: ThingId, contributorId: ContributorId) =
            UpdateComparisonUseCase.UpdateComparisonRelatedFigureCommand(
                comparisonId, comparisonRelatedFigureId, contributorId, label, image, description
            )
    }

    data class PublishRequest(
        @field:NotBlank
        val subject: String,
        @field:NotBlank
        val description: String,
        @field:Valid
        @field:Size(min = 1)
        val authors: List<AuthorDTO>
    ) {
        fun toPublishCommand(id: ThingId, contributorId: ContributorId): PublishComparisonUseCase.PublishCommand =
            PublishComparisonUseCase.PublishCommand(
                id = id,
                contributorId = contributorId,
                subject = subject,
                description = description,
                authors = authors.map { it.toAuthor() }
            )
    }
}
