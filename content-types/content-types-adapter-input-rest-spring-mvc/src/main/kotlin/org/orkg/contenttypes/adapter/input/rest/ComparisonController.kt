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
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRepresentationAdapter
import org.orkg.contenttypes.domain.ComparisonConfig
import org.orkg.contenttypes.domain.ComparisonData
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.PublishComparisonUseCase
import org.orkg.contenttypes.input.UpdateComparisonUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
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

const val COMPARISON_JSON_V2 = "application/vnd.orkg.comparison.v2+json"

@RestController
@RequestMapping("/api/comparisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class ComparisonController(
    private val service: ComparisonUseCases,
) : ComparisonRepresentationAdapter {
    @GetMapping("/{id}", produces = [COMPARISON_JSON_V2])
    fun findById(
        @PathVariable id: ThingId,
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
        @RequestParam("published", required = false) published: Boolean?,
        @RequestParam("research_problem", required = false) researchProblem: ThingId?,
        pageable: Pageable,
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
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            researchProblem = researchProblem
        ).mapToComparisonRepresentation()

    @RequireLogin
    @PostMapping(consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun create(
        @RequestBody @Valid request: CreateComparisonRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [COMPARISON_JSON_V2], produces = [COMPARISON_JSON_V2])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateComparisonRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping("/{id}/publish", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishComparisonRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val comparisonVersionId = service.publish(request.toPublishCommand(id, contributorId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}")
            .buildAndExpand(comparisonVersionId)
            .toUri()
        return created(location).build()
    }

    data class CreateComparisonRequest(
        @field:NotBlank
        val title: String,
        val description: String,
        @field:Size(max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        @field:Valid
        val authors: List<AuthorRequest>,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val contributions: List<ThingId>,
        val config: ComparisonConfig,
        val data: ComparisonData,
        val visualizations: List<ThingId>?,
        val references: List<String>,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("is_anonymized")
        val isAnonymized: Boolean,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
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
                config = config,
                data = data,
                visualizations = visualizations.orEmpty(),
                references = references,
                observatories = observatories,
                organizations = organizations,
                isAnonymized = isAnonymized,
                extractionMethod = extractionMethod
            )
    }

    data class UpdateComparisonRequest(
        @field:NullableNotBlank
        val title: String?,
        @field:NullableNotBlank
        val description: String?,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>?,
        @field:Valid
        val authors: List<AuthorRequest>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val contributions: List<ThingId>?,
        val config: ComparisonConfig?,
        val data: ComparisonData?,
        val visualizations: List<ThingId>?,
        val references: List<String>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("is_anonymized")
        val isAnonymized: Boolean?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
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
                config = config,
                data = data,
                visualizations = visualizations,
                references = references,
                observatories = observatories,
                organizations = organizations,
                isAnonymized = isAnonymized,
                extractionMethod = extractionMethod,
                visibility = visibility
            )
    }

    data class PublishComparisonRequest(
        @field:NotBlank
        val subject: String,
        @field:NotBlank
        val description: String,
        @field:Valid
        @field:Size(min = 1)
        val authors: List<AuthorRequest>,
        @JsonProperty("assign_doi")
        val assignDOI: Boolean,
    ) {
        fun toPublishCommand(id: ThingId, contributorId: ContributorId): PublishComparisonUseCase.PublishCommand =
            PublishComparisonUseCase.PublishCommand(
                id = id,
                contributorId = contributorId,
                subject = subject,
                description = description,
                authors = authors.map { it.toAuthor() },
                assignDOI = assignDOI
            )
    }
}
