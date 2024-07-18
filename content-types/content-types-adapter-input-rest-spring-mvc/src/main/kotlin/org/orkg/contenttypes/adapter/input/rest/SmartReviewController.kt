package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
import org.orkg.contenttypes.adapter.input.rest.mapping.SmartReviewRepresentationAdapter
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.input.CreateSmartReviewUseCase
import org.orkg.contenttypes.input.SmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.SmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.SmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.SmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionCommand
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionCommand
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
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

const val SMART_REVIEW_JSON_V1 = "application/vnd.orkg.smart-review.v1+json"

@RestController
@RequestMapping("/api/smart-reviews", produces = [SMART_REVIEW_JSON_V1])
class SmartReviewController(
    private val service: SmartReviewUseCases
) : SmartReviewRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): SmartReviewRepresentation = service.findById(id)
        .mapToSmartReviewRepresentation()
        .orElseThrow { SmartReviewNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("title", required = false) title: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        @RequestParam("published", required = false) published: Boolean?,
        @RequestParam("sdg", required = false) sustainableDevelopmentGoal: ThingId?,
        pageable: Pageable
    ): Page<SmartReviewRepresentation> =
        service.findAll(
            pageable = pageable,
            label = title?.let { SearchString.of(title, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).mapToSmartReviewRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [SMART_REVIEW_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateSmartReviewRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/smart-reviews/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    data class CreateSmartReviewRequest(
        @field:NotBlank
        val title: String,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        @field:Valid
        val authors: List<AuthorDTO>,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
        @field:Valid
        val sections: List<SmartReviewSectionRequest>,
        @field:Valid
        val references: List<@NotBlank String>
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateSmartReviewUseCase.CreateCommand =
            CreateSmartReviewUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                authors = authors.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                sections = sections.map { it.toSmartReviewSectionDefinition() },
                references = references
            )
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(value = [
        JsonSubTypes.Type(SmartReviewComparisonSectionRequest::class),
        JsonSubTypes.Type(SmartReviewVisualizationSection::class),
        JsonSubTypes.Type(SmartReviewResourceSectionRequest::class),
        JsonSubTypes.Type(SmartReviewPredicateSectionRequest::class),
        JsonSubTypes.Type(SmartReviewOntologySectionRequest::class),
        JsonSubTypes.Type(SmartReviewTextSectionRequest::class),
    ])
    sealed interface SmartReviewSectionRequest {
        val heading: String

        fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition
    }

    data class SmartReviewComparisonSectionRequest(
        override val heading: String,
        val comparison: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewComparisonSectionCommand(heading, comparison)
    }

    data class SmartReviewVisualizationSection(
        override val heading: String,
        val visualization: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewVisualizationSectionCommand(heading, visualization)
    }

    data class SmartReviewResourceSectionRequest(
        override val heading: String,
        val resource: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewResourceSectionCommand(heading, resource)
    }

    data class SmartReviewPredicateSectionRequest(
        override val heading: String,
        val predicate: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewPredicateSectionCommand(heading, predicate)
    }

    data class SmartReviewOntologySectionRequest(
        override val heading: String,
        val entities: List<ThingId>,
        val predicates: List<ThingId>
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewOntologySectionCommand(heading, entities, predicates)
    }

    data class SmartReviewTextSectionRequest(
        override val heading: String,
        val `class`: ThingId,
        val text: String
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewTextSectionCommand(heading, `class`, text)
    }
}
