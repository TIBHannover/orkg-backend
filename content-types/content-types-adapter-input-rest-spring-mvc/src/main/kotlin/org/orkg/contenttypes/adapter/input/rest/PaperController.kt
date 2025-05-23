package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import dev.forkhandles.values.ofOrNull
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.DOI
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.mapping.ContributionRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.PaperRepresentationAdapter
import org.orkg.contenttypes.domain.InvalidDOI
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperWithStatementCount
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.contenttypes.input.UpdatePaperUseCase
import org.orkg.graph.domain.ExactSearchString
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
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

const val PAPER_JSON_V2 = "application/vnd.orkg.paper.v2+json"

@RestController
@RequestMapping("/api/papers", produces = [PAPER_JSON_V2])
class PaperController(
    private val service: PaperUseCases,
) : PaperRepresentationAdapter,
    ContributionRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
    ): PaperRepresentation = service.findById(id)
        .mapToPaperRepresentation()
        .orElseThrow { PaperNotFound(id) }

    @GetMapping("/{id}/contributors", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAllContributorsByPaperId(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<ContributorId> = service.findAllContributorsByPaperId(id, pageable)

    @GetMapping("/statement-counts")
    fun countAllStatementsAboutPapers(
        pageable: Pageable,
    ): Page<PaperWithStatementCount> = service.countAllStatementsAboutPapers(pageable)

    @GetMapping
    fun findAll(
        @RequestParam("title", required = false) title: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("doi", required = false) doi: String?,
        @RequestParam("doi_prefix", required = false) doiPrefix: String?,
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
        @RequestParam("mentionings", required = false) mentionings: Set<ThingId>?,
        pageable: Pageable,
    ): Page<PaperRepresentation> =
        service.findAll(
            pageable = pageable,
            doi = doi?.also { DOI.ofOrNull(doi) ?: throw InvalidDOI(doi) },
            doiPrefix = doiPrefix,
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
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            mentionings = mentionings
        ).mapToPaperRepresentation()

    @RequireLogin
    @PostMapping(consumes = [PAPER_JSON_V2])
    fun create(
        @RequestBody @Valid request: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<PaperRepresentation> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [PAPER_JSON_V2])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<PaperRepresentation> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping("/{id}/publish", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val contributorId = currentUser.contributorId()
        val paperVersionId = service.publish(request.toPublishCommand(id, contributorId))
        val location = uriComponentsBuilder
            .path("/api/resources/{id}")
            .buildAndExpand(paperVersionId)
            .toUri()
        return created(location).build()
    }

    @RequestMapping(method = [RequestMethod.HEAD], params = ["doi"])
    fun existsByDOI(
        @RequestParam("doi", required = false) doi: String,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Nothing> = service.existsByDOI(DOI.ofOrNull(doi) ?: throw InvalidDOI(doi))
        .map {
            val location = uriComponentsBuilder.path("/api/papers/{id}")
                .buildAndExpand(it)
                .toUri()
            ok().location(location).build<Nothing>()
        }
        .orElseGet { notFound().build() }

    @RequestMapping(method = [RequestMethod.HEAD], params = ["title"])
    fun existsByTitle(
        @RequestParam("title", required = false) title: String,
        uriComponentsBuilder: UriComponentsBuilder,
    ): ResponseEntity<Nothing> =
        service.existsByTitle(ExactSearchString(title))
            .map {
                val location = uriComponentsBuilder.path("/api/papers/{id}")
                    .buildAndExpand(it)
                    .toUri()
                ok().location(location).build<Nothing>()
            }
            .orElseGet { notFound().build() }

    data class CreatePaperRequest(
        @field:NotBlank
        val title: String,
        @field:Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        @field:Valid
        val identifiers: IdentifierMapRequest?,
        @JsonProperty("publication_info")
        val publicationInfo: PublicationInfoRequest?,
        @field:Valid
        val authors: List<AuthorRequest>,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val mentionings: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @field:Valid
        val contents: PaperContentsRequest?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        data class PaperContentsRequest(
            @field:Valid
            val resources: Map<String, CreateResourceRequestPart>?,
            @field:Valid
            val literals: Map<String, CreateLiteralRequestPart>?,
            @field:Valid
            val predicates: Map<String, CreatePredicateRequestPart>?,
            @field:Valid
            val lists: Map<String, CreateListRequestPart>?,
            @field:Valid
            val contributions: List<ContributionRequestPart>,
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

        data class ContributionRequestPart(
            @field:NotBlank
            val label: String,
            val classes: Set<ThingId>?,
            @field:Valid
            @field:Size(min = 1)
            val statements: Map<String, List<StatementObjectRequest>>,
        ) {
            fun toCreateCommand(): CreateContributionCommandPart =
                CreateContributionCommandPart(
                    label = label,
                    classes = classes.orEmpty(),
                    statements = statements.mapValues { it.value.map { statement -> statement.toCreateCommand() } }
                )

            data class StatementObjectRequest(
                val id: String,
                @field:Valid
                @field:Size(min = 1)
                val statements: Map<String, List<StatementObjectRequest>>?,
            ) {
                fun toCreateCommand(): CreateContributionCommandPart.StatementObject =
                    CreateContributionCommandPart.StatementObject(
                        id = id,
                        statements = statements?.mapValues { it.value.map { statement -> statement.toCreateCommand() } }
                    )
            }
        }

        fun toCreateCommand(contributorId: ContributorId): CreatePaperUseCase.CreateCommand =
            CreatePaperUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                identifiers = identifiers?.values.orEmpty(),
                publicationInfo = publicationInfo?.toPublicationInfoCommand(),
                authors = authors.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals.orEmpty(),
                mentionings = mentionings.orEmpty(),
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
        @field:Valid
        val identifiers: IdentifierMapRequest?,
        @field:Valid
        @JsonProperty("publication_info")
        val publicationInfo: PublicationInfoRequest?,
        @field:Valid
        val authors: List<AuthorRequest>?,
        @JsonProperty("sdgs")
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val mentionings: Set<ThingId>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
        val verified: Boolean?,
    ) {
        fun toUpdateCommand(paperId: ThingId, contributorId: ContributorId): UpdatePaperUseCase.UpdateCommand =
            UpdatePaperUseCase.UpdateCommand(
                paperId = paperId,
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                identifiers = identifiers?.values,
                publicationInfo = publicationInfo?.toPublicationInfoCommand(),
                authors = authors?.map { it.toAuthor() },
                sustainableDevelopmentGoals = sustainableDevelopmentGoals,
                mentionings = mentionings,
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                visibility = visibility,
                verified = verified
            )
    }

    data class PublishRequest(
        @field:NotBlank
        val subject: String,
        @field:NotBlank
        val description: String,
        @field:Valid
        @field:Size(min = 1)
        val authors: List<AuthorRequest>,
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
