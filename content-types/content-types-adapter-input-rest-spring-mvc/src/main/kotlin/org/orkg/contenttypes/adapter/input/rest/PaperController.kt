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
import org.orkg.contenttypes.adapter.input.rest.mapping.ContributionRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.PaperRepresentationAdapter
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperWithStatementCount
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.contenttypes.input.UpdatePaperUseCase
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

const val PAPER_JSON_V2 = "application/vnd.orkg.paper.v2+json"

@RestController
@RequestMapping("/api/papers", produces = [PAPER_JSON_V2])
class PaperController(
    private val service: PaperUseCases
) : PaperRepresentationAdapter, ContributionRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): PaperRepresentation = service.findById(id)
        .mapToPaperRepresentation()
        .orElseThrow { PaperNotFound(id) }

    @GetMapping("/{id}/contributors", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAllContributorsByPaperId(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ContributorId> = service.findAllContributorsByPaperId(id, pageable)

    @GetMapping("/statement-counts")
    fun countAllStatementsAboutPapers(
        pageable: Pageable
    ): Page<PaperWithStatementCount> = service.countAllStatementsAboutPapers(pageable)

    @GetMapping
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
    ): Page<PaperRepresentation> =
        service.findAll(
            pageable = pageable,
            doi = doi,
            label = title?.let { SearchString.of(title, exactMatch) },
            visibility = visibility,
            verified = verified,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal
        ).mapToPaperRepresentation()

    @PreAuthorizeUser
    @PostMapping(consumes = [PAPER_JSON_V2])
    fun create(
        @RequestBody @Valid request: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<PaperRepresentation> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [PAPER_JSON_V2])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<PaperRepresentation> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @PreAuthorizeUser
    @PostMapping("/{id}/contributions", produces = [CONTRIBUTION_JSON_V2], consumes = [CONTRIBUTION_JSON_V2])
    fun createContribution(
        @PathVariable("id") paperId: ThingId,
        @RequestBody @Valid request: CreateContributionRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<ContributionRepresentation> {
        val userId = currentUser.contributorId()
        val id = service.createContribution(request.toCreateCommand(userId, paperId))
        val location = uriComponentsBuilder
            .path("api/contributions/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PreAuthorizeUser
    @PostMapping("/{id}/publish", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        service.publish(request.toPublishCommand(id, contributorId))
        val location = uriComponentsBuilder
            .path("api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreatePaperRequest(
        @field:NotBlank
        val title: String,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        @field: Valid
        val identifiers: IdentifierMapDTO?,
        @JsonProperty("publication_info")
        val publicationInfo: PublicationInfoDTO?,
        @field:Valid
        val authors: List<AuthorDTO>,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @field:Valid
        val contents: PaperContentsDTO?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        data class PaperContentsDTO(
            @field:Valid
            val resources: Map<String, ResourceDefinitionDTO>?,
            @field:Valid
            val literals: Map<String, LiteralDefinitionDTO>?,
            @field:Valid
            val predicates: Map<String, PredicateDefinitionDTO>?,
            @field:Valid
            val lists: Map<String, ListDefinitionDTO>?,
            @field:Valid
            val contributions: List<ContributionDTO>
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.PaperContents =
                CreatePaperUseCase.CreateCommand.PaperContents(
                    resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                    literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                    predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                    lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                    contributions = contributions.map { it.toCreateCommand() }
                )
        }

        data class ContributionDTO(
            @field:NotBlank
            val label: String,
            val classes: Set<ThingId>?,
            @field:Valid
            @field:Size(min = 1)
            val statements: Map<String, List<StatementObjectDefinitionDTO>>
        ) {
            fun toCreateCommand(): ContributionDefinition =
                ContributionDefinition(
                    label = label,
                    classes = classes.orEmpty(),
                    statements = statements.mapValues { it.value.map { statement -> statement.toCreateCommand() } }
                )
        }

        data class StatementObjectDefinitionDTO(
            val id: String,
            @field:Valid
            @field:Size(min = 1)
            val statements: Map<String, List<StatementObjectDefinitionDTO>>?
        ) {
            fun toCreateCommand(): ContributionDefinition.StatementObjectDefinition =
                ContributionDefinition.StatementObjectDefinition(
                    id = id,
                    statements = statements?.mapValues { it.value.map { statement -> statement.toCreateCommand() } }
                )
        }

        fun toCreateCommand(contributorId: ContributorId): CreatePaperUseCase.CreateCommand =
            CreatePaperUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                identifiers = identifiers?.values.orEmpty(),
                publicationInfo = publicationInfo?.toPublicationInfoDefinition(),
                authors = authors.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty(),
                observatories = observatories,
                organizations = organizations,
                contents = contents?.toCreateCommand(),
                extractionMethod = extractionMethod
            )
    }

    data class UpdatePaperRequest(
        @field:Size(min = 1)
        val title: String?,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>?,
        @field: Valid
        val identifiers: IdentifierMapDTO?,
        @field:Valid
        @JsonProperty("publication_info")
        val publicationInfo: PublicationInfoDTO?,
        @field:Valid
        val authors: List<AuthorDTO>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?
    ) {
        fun toUpdateCommand(paperId: ThingId, contributorId: ContributorId): UpdatePaperUseCase.UpdateCommand =
            UpdatePaperUseCase.UpdateCommand(
                paperId = paperId,
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                identifiers = identifiers?.values,
                publicationInfo = publicationInfo?.toPublicationInfoDefinition(),
                authors = authors?.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
                observatories = observatories,
                organizations = organizations
            )
    }

    data class CreateContributionRequest(
        @field:Valid
        val resources: Map<String, ResourceDefinitionDTO>?,
        @field:Valid
        val literals: Map<String, LiteralDefinitionDTO>?,
        @field:Valid
        val predicates: Map<String, PredicateDefinitionDTO>?,
        @field:Valid
        val lists: Map<String, ListDefinitionDTO>?,
        @field:Valid
        val contribution: CreatePaperRequest.ContributionDTO,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
    ) {
        fun toCreateCommand(contributorId: ContributorId, paperId: ThingId): CreateContributionUseCase.CreateCommand =
            CreateContributionUseCase.CreateCommand(
                contributorId = contributorId,
                paperId = paperId,
                extractionMethod = extractionMethod,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                contribution = contribution.toCreateCommand()
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
        fun toPublishCommand(id: ThingId, contributorId: ContributorId): PublishPaperUseCase.PublishCommand =
            PublishPaperUseCase.PublishCommand(
                id = id,
                contributorId = contributorId,
                subject = subject,
                description = description,
                authors = authors.map { it.toAuthor() }
            )
    }
}
