package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.LiteratureListRepresentationAdapter
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.input.CreateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.CreateLiteratureListUseCase
import org.orkg.contenttypes.input.ListSectionCommand
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.input.TextSectionCommand
import org.orkg.contenttypes.input.UpdateLiteratureListUseCase
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

const val LITERATURE_LIST_JSON_V1 = "application/vnd.orkg.literature-list.v1+json"
const val LITERATURE_LIST_SECTION_JSON_V1 = "application/vnd.orkg.literature-list-section.v1+json"

@RestController
@RequestMapping("/api/literature-lists", produces = [LITERATURE_LIST_JSON_V1])
class LiteratureListController(
    private val service: LiteratureListUseCases
) : LiteratureListRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): LiteratureListRepresentation = service.findById(id)
        .mapToLiteratureListRepresentation()
        .orElseThrow { LiteratureListNotFound(id) }

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
    ): Page<LiteratureListRepresentation> =
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
        ).mapToLiteratureListRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [LITERATURE_LIST_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateLiteratureListRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [LITERATURE_LIST_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateLiteratureListRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @PreAuthorizeUser
    @PostMapping("/{id}/sections", consumes = [LITERATURE_LIST_SECTION_JSON_V1], produces = [LITERATURE_LIST_SECTION_JSON_V1])
    fun createSection(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: LiteratureListSectionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.createSection(request.toCreateCommand(userId, id))
        val location = uriComponentsBuilder
            .path("api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    data class CreateLiteratureListRequest(
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
        val sections: List<LiteratureListSectionRequest>
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateLiteratureListUseCase.CreateCommand =
            CreateLiteratureListUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                authors = authors.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                sections = sections.map { it.toLiteratureListSectionDefinition() }
            )
    }

    data class UpdateLiteratureListRequest(
        @field:NotBlank
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
        val sections: List<LiteratureListSectionRequest>?
    ) {
        fun toUpdateCommand(literatureListId: ThingId, contributorId: ContributorId): UpdateLiteratureListUseCase.UpdateCommand =
            UpdateLiteratureListUseCase.UpdateCommand(
                literatureListId = literatureListId,
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                authors = authors?.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                sections = sections?.map { it.toLiteratureListSectionDefinition() }
            )
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(value = [
        JsonSubTypes.Type(ListSectionRequest::class),
        JsonSubTypes.Type(TextSectionRequest::class)
    ])
    sealed interface LiteratureListSectionRequest {
        fun toLiteratureListSectionDefinition(): LiteratureListSectionDefinition

        fun toCreateCommand(
            contributorId: ContributorId,
            literatureListId: ThingId
        ): CreateLiteratureListSectionUseCase.CreateCommand
    }

    data class ListSectionRequest(
        val entries: List<ThingId>
    ) : LiteratureListSectionRequest {
        override fun toLiteratureListSectionDefinition(): LiteratureListSectionDefinition =
            ListSectionCommand(entries)

        override fun toCreateCommand(
            contributorId: ContributorId,
            literatureListId: ThingId
        ): CreateLiteratureListSectionUseCase.CreateCommand =
            CreateLiteratureListSectionUseCase.CreateListSectionCommand(contributorId, literatureListId, entries)
    }

    data class TextSectionRequest(
        @field:NullableNotBlank
        val heading: String,
        @field:Positive
        @JsonProperty("heading_size")
        val headingSize: Int,
        @field:NullableNotBlank
        val text: String
    ) : LiteratureListSectionRequest {
        override fun toLiteratureListSectionDefinition(): LiteratureListSectionDefinition =
            TextSectionCommand(heading, headingSize, text)

        override fun toCreateCommand(
            contributorId: ContributorId,
            literatureListId: ThingId
        ): CreateLiteratureListSectionUseCase.CreateCommand =
            CreateLiteratureListSectionUseCase.CreateTextSectionCommand(
                contributorId, literatureListId, heading, headingSize, text
            )
    }
}
