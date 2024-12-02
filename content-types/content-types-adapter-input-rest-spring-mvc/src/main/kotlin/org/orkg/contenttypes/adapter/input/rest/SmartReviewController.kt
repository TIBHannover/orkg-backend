package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.SmartReviewRepresentationAdapter
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.input.CreateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.CreateSmartReviewUseCase
import org.orkg.contenttypes.input.DeleteSmartReviewSectionUseCase
import org.orkg.contenttypes.input.PublishSmartReviewUseCase
import org.orkg.contenttypes.input.SmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.SmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.SmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.SmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionCommand
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionCommand
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase
import org.orkg.contenttypes.input.UpdateSmartReviewUseCase
import org.orkg.graph.domain.ExtractionMethod
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

const val SMART_REVIEW_JSON_V1 = "application/vnd.orkg.smart-review.v1+json"
const val SMART_REVIEW_SECTION_JSON_V1 = "application/vnd.orkg.smart-review-section.v1+json"

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
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
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
            researchField = researchField,
            includeSubfields = includeSubfields,
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).mapToSmartReviewRepresentation()

    @RequireLogin
    @PostMapping(consumes = [SMART_REVIEW_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateSmartReviewRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/smart-reviews/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PostMapping(value = ["/{id}/sections", "/{id}/sections/{index}"], consumes = [SMART_REVIEW_SECTION_JSON_V1], produces = [SMART_REVIEW_SECTION_JSON_V1])
    fun createSection(
        @PathVariable id: ThingId,
        @PathVariable(required = false) @PositiveOrZero index: Int?,
        @RequestBody @Valid request: SmartReviewSectionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.createSection(request.toCreateCommand(userId, id, index))
        val location = uriComponentsBuilder
            .path("/api/smart-reviews/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [SMART_REVIEW_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateSmartReviewRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/smart-reviews/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PutMapping("/{smartReviewId}/sections/{sectionId}", consumes = [SMART_REVIEW_SECTION_JSON_V1], produces = [SMART_REVIEW_SECTION_JSON_V1])
    fun updateSection(
        @PathVariable smartReviewId: ThingId,
        @PathVariable sectionId: ThingId,
        @RequestBody @Valid request: SmartReviewSectionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.updateSection(request.toUpdateCommand(sectionId, userId, smartReviewId))
        val location = uriComponentsBuilder
            .path("/api/smart-reviews/{id}")
            .buildAndExpand(smartReviewId)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @DeleteMapping("/{smartReviewId}/sections/{sectionId}", produces = [SMART_REVIEW_SECTION_JSON_V1])
    fun deleteSection(
        @PathVariable smartReviewId: ThingId,
        @PathVariable sectionId: ThingId,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.deleteSection(DeleteSmartReviewSectionUseCase.DeleteCommand(smartReviewId, sectionId, userId))
        val location = uriComponentsBuilder
            .path("/api/smart-reviews/{id}")
            .buildAndExpand(smartReviewId)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping("/{id}/publish", produces = [SMART_REVIEW_JSON_V1])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val smartReviewId = service.publish(request.toPublishCommand(id, contributorId))
        val location = uriComponentsBuilder
            .path("/api/smart-reviews/{id}")
            .buildAndExpand(smartReviewId)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateSmartReviewRequest(
        @field:NotBlank
        val title: String,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        @field:Valid
        val authors: List<AuthorDTO>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
        @field:Valid
        val sections: List<SmartReviewSectionRequest>?,
        @field:Valid
        val references: List<@NotBlank String>?
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateSmartReviewUseCase.CreateCommand =
            CreateSmartReviewUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                authors = authors?.map { it.toAuthor() }.orEmpty(),
                sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty(),
                observatories = observatories.orEmpty(),
                organizations = organizations.orEmpty(),
                extractionMethod = extractionMethod,
                sections = sections?.map { it.toSmartReviewSectionDefinition() }.orEmpty(),
                references = references.orEmpty()
            )
    }

    data class UpdateSmartReviewRequest(
        @field:NullableNotBlank
        val title: String?,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>?,
        @field:Valid
        val authors: List<AuthorDTO>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        @field:Valid
        val sections: List<SmartReviewSectionRequest>?,
        @field:Valid
        val references: List<@NotBlank String>?
    ) {
        fun toUpdateCommand(smartReviewId: ThingId, contributorId: ContributorId): UpdateSmartReviewUseCase.UpdateCommand =
            UpdateSmartReviewUseCase.UpdateCommand(
                smartReviewId = smartReviewId,
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                authors = authors?.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                sections = sections?.map { it.toSmartReviewSectionDefinition() },
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

        fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand

        fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand
    }

    data class SmartReviewComparisonSectionRequest(
        override val heading: String,
        val comparison: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewComparisonSectionCommand(heading, comparison)

        override fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand =
            CreateSmartReviewSectionUseCase.CreateComparisonSectionCommand(
                contributorId, smartReviewId, index, heading, comparison
            )

        override fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand =
            UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand(
                smartReviewSectionId, contributorId, smartReviewId, heading, comparison
            )
    }

    data class SmartReviewVisualizationSection(
        override val heading: String,
        val visualization: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewVisualizationSectionCommand(heading, visualization)

        override fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand =
            CreateSmartReviewSectionUseCase.CreateVisualizationSectionCommand(
                contributorId, smartReviewId, index, heading, visualization
            )

        override fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand =
            UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand(
                smartReviewSectionId, contributorId, smartReviewId, heading, visualization
            )
    }

    data class SmartReviewResourceSectionRequest(
        override val heading: String,
        val resource: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewResourceSectionCommand(heading, resource)

        override fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand =
            CreateSmartReviewSectionUseCase.CreateResourceSectionCommand(
                contributorId, smartReviewId, index, heading, resource
            )

        override fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand =
            UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand(
                smartReviewSectionId, contributorId, smartReviewId, heading, resource
            )
    }

    data class SmartReviewPredicateSectionRequest(
        override val heading: String,
        val predicate: ThingId?
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewPredicateSectionCommand(heading, predicate)

        override fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand =
            CreateSmartReviewSectionUseCase.CreatePredicateSectionCommand(
                contributorId, smartReviewId, index, heading, predicate
            )

        override fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand =
            UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand(
                smartReviewSectionId, contributorId, smartReviewId, heading, predicate
            )
    }

    data class SmartReviewOntologySectionRequest(
        override val heading: String,
        val entities: List<ThingId>,
        val predicates: List<ThingId>
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewOntologySectionCommand(heading, entities, predicates)

        override fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand =
            CreateSmartReviewSectionUseCase.CreateOntologySectionCommand(
                contributorId, smartReviewId, index, heading, entities, predicates
            )

        override fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand =
            UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand(
                smartReviewSectionId, contributorId, smartReviewId, heading, entities, predicates
            )
    }

    data class SmartReviewTextSectionRequest(
        override val heading: String,
        val `class`: ThingId?,
        val text: String
    ) : SmartReviewSectionRequest {
        override fun toSmartReviewSectionDefinition(): SmartReviewSectionDefinition =
            SmartReviewTextSectionCommand(heading, `class`, text)

        override fun toCreateCommand(
            contributorId: ContributorId,
            smartReviewId: ThingId,
            index: Int?
        ): CreateSmartReviewSectionUseCase.CreateCommand =
            CreateSmartReviewSectionUseCase.CreateTextSectionCommand(
                contributorId, smartReviewId, index, heading, `class`, text
            )

        override fun toUpdateCommand(
            smartReviewSectionId: ThingId,
            contributorId: ContributorId,
            smartReviewId: ThingId
        ): UpdateSmartReviewSectionUseCase.UpdateCommand =
            UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand(
                smartReviewSectionId, contributorId, smartReviewId, heading, `class`, text
            )
    }

    data class PublishRequest(
        @field:NotBlank
        val changelog: String,
        @JsonProperty("assign_doi")
        val assignDOI: Boolean,
        @field:NullableNotBlank
        val description: String?
    ) {
        fun toPublishCommand(id: ThingId, contributorId: ContributorId): PublishSmartReviewUseCase.PublishCommand =
            PublishSmartReviewUseCase.PublishCommand(id, contributorId, changelog, assignDOI, description)
    }
}
