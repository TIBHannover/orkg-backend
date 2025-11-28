package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.LiteratureListRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.PaperRepresentationAdapter
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.input.AbstractLiteratureListListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListSectionCommand
import org.orkg.contenttypes.input.CreateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.CreateLiteratureListUseCase
import org.orkg.contenttypes.input.DeleteLiteratureListSectionUseCase
import org.orkg.contenttypes.input.LiteratureListListSectionCommand
import org.orkg.contenttypes.input.LiteratureListTextSectionCommand
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.contenttypes.input.PublishLiteratureListUseCase
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.UpdateLiteratureListUseCase
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
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
import java.util.Optional

const val LITERATURE_LIST_JSON_V1 = "application/vnd.orkg.literature-list.v1+json"
const val LITERATURE_LIST_SECTION_JSON_V1 = "application/vnd.orkg.literature-list-section.v1+json"

@RestController
@RequestMapping("/api/literature-lists", produces = [LITERATURE_LIST_JSON_V1])
class LiteratureListController(
    private val service: LiteratureListUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
    override val statementService: StatementUseCases,
) : LiteratureListRepresentationAdapter,
    ResourceRepresentationAdapter,
    PaperRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
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
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        @RequestParam("published", required = false) published: Boolean?,
        @RequestParam("sdg", required = false) sustainableDevelopmentGoal: ThingId?,
        pageable: Pageable,
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
            researchField = researchField,
            includeSubfields = includeSubfields,
            published = published,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).mapToLiteratureListRepresentation()

    @GetMapping("/{id}/published-contents/{contentId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findPublishedContentById(
        @PathVariable id: ThingId,
        @PathVariable contentId: ThingId,
        capabilities: MediaTypeCapabilities,
    ): ResponseEntity<Any> =
        service.findPublishedContentById(id, contentId)
            .fold({ it.toPaperRepresentation() }, { Optional.of(it).mapToResourceRepresentation(capabilities).get() })
            .let(::ok)

    @RequireLogin
    @PostMapping(consumes = [LITERATURE_LIST_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateLiteratureListRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [LITERATURE_LIST_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateLiteratureListRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping(value = ["/{id}/sections", "/{id}/sections/{index}"], consumes = [LITERATURE_LIST_SECTION_JSON_V1], produces = [LITERATURE_LIST_SECTION_JSON_V1])
    fun createSection(
        @PathVariable id: ThingId,
        @PathVariable(required = false) @PositiveOrZero index: Int?,
        @RequestBody @Valid request: LiteratureListSectionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.create(request.toCreateCommand(userId, id, index))
        val location = uriComponentsBuilder
            .path("/api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}/sections/{sectionId}", consumes = [LITERATURE_LIST_SECTION_JSON_V1], produces = [LITERATURE_LIST_SECTION_JSON_V1])
    fun updateSection(
        @PathVariable id: ThingId,
        @PathVariable sectionId: ThingId,
        @RequestBody @Valid request: LiteratureListSectionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(sectionId, userId, id))
        val location = uriComponentsBuilder
            .path("/api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}/sections/{sectionId}", produces = [LITERATURE_LIST_SECTION_JSON_V1])
    fun deleteSection(
        @PathVariable id: ThingId,
        @PathVariable sectionId: ThingId,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.delete(DeleteLiteratureListSectionUseCase.DeleteCommand(id, sectionId, userId))
        val location = uriComponentsBuilder
            .path("/api/literature-lists/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping("/{id}/publish", consumes = [LITERATURE_LIST_JSON_V1])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishLiteratureListRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val literatureListVersionId = service.publish(request.toPublishCommand(id, contributorId))
        val location = uriComponentsBuilder
            .path("/api/literature-lists/{id}")
            .buildAndExpand(literatureListVersionId)
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
        val authors: List<AuthorRequest>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
        @field:Valid
        val sections: List<LiteratureListSectionRequest>?,
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateLiteratureListUseCase.CreateCommand =
            CreateLiteratureListUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                authors = authors?.map { it.toAuthor() }.orEmpty(),
                sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty(),
                observatories = observatories.orEmpty(),
                organizations = organizations.orEmpty(),
                extractionMethod = extractionMethod,
                sections = sections?.map { it.toLiteratureListSectionCommand() }.orEmpty()
            )
    }

    data class UpdateLiteratureListRequest(
        @field:NullableNotBlank
        val title: String?,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>?,
        @field:Valid
        val authors: List<AuthorRequest>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        @field:Valid
        val sections: List<LiteratureListSectionRequest>?,
        val visibility: Visibility?,
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
                sections = sections?.map { it.toLiteratureListSectionCommand() },
                visibility = visibility,
            )
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(
        value = [
            JsonSubTypes.Type(LiteratureListListSectionRequest::class),
            JsonSubTypes.Type(LiteratureListTextSectionRequest::class)
        ]
    )
    sealed interface LiteratureListSectionRequest {
        fun toLiteratureListSectionCommand(): AbstractLiteratureListSectionCommand

        fun toCreateCommand(
            contributorId: ContributorId,
            literatureListId: ThingId,
            index: Int?,
        ): CreateLiteratureListSectionUseCase.CreateCommand

        fun toUpdateCommand(
            literatureListSectionId: ThingId,
            contributorId: ContributorId,
            literatureListId: ThingId,
        ): UpdateLiteratureListSectionUseCase.UpdateCommand
    }

    data class LiteratureListListSectionRequest(
        val entries: List<Entry>,
    ) : LiteratureListSectionRequest {
        data class Entry(
            val id: ThingId,
            val description: String? = null,
        ) {
            fun toCommandEntry(): AbstractLiteratureListListSectionCommand.Entry =
                AbstractLiteratureListListSectionCommand.Entry(id, description)
        }

        override fun toLiteratureListSectionCommand(): AbstractLiteratureListSectionCommand =
            LiteratureListListSectionCommand(entries.map { it.toCommandEntry() })

        override fun toCreateCommand(
            contributorId: ContributorId,
            literatureListId: ThingId,
            index: Int?,
        ): CreateLiteratureListSectionUseCase.CreateCommand =
            CreateLiteratureListSectionUseCase.CreateListSectionCommand(
                contributorId,
                literatureListId,
                index,
                entries.map { it.toCommandEntry() }
            )

        override fun toUpdateCommand(
            literatureListSectionId: ThingId,
            contributorId: ContributorId,
            literatureListId: ThingId,
        ): UpdateLiteratureListSectionUseCase.UpdateCommand =
            UpdateLiteratureListSectionUseCase.UpdateListSectionCommand(
                literatureListSectionId,
                contributorId,
                literatureListId,
                entries.map { it.toCommandEntry() }
            )
    }

    data class LiteratureListTextSectionRequest(
        val heading: String,
        @field:Positive
        @JsonProperty("heading_size")
        val headingSize: Int,
        val text: String,
    ) : LiteratureListSectionRequest {
        override fun toLiteratureListSectionCommand(): AbstractLiteratureListSectionCommand =
            LiteratureListTextSectionCommand(heading, headingSize, text)

        override fun toCreateCommand(
            contributorId: ContributorId,
            literatureListId: ThingId,
            index: Int?,
        ): CreateLiteratureListSectionUseCase.CreateCommand =
            CreateLiteratureListSectionUseCase.CreateTextSectionCommand(
                contributorId,
                literatureListId,
                index,
                heading,
                headingSize,
                text
            )

        override fun toUpdateCommand(
            literatureListSectionId: ThingId,
            contributorId: ContributorId,
            literatureListId: ThingId,
        ): UpdateLiteratureListSectionUseCase.UpdateCommand =
            UpdateLiteratureListSectionUseCase.UpdateTextSectionCommand(
                literatureListSectionId,
                contributorId,
                literatureListId,
                heading,
                headingSize,
                text
            )
    }

    data class PublishLiteratureListRequest(
        @field:NotBlank
        val changelog: String,
    ) {
        fun toPublishCommand(id: ThingId, contributorId: ContributorId): PublishLiteratureListUseCase.PublishCommand =
            PublishLiteratureListUseCase.PublishCommand(
                id = id,
                contributorId = contributorId,
                changelog = changelog
            )
    }
}
