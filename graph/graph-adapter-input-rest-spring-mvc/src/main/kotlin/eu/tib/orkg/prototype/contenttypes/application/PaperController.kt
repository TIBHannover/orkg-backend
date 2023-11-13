package eu.tib.orkg.prototype.contenttypes.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.ContributionRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.PaperRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.api.ContributionRepresentation
import eu.tib.orkg.prototype.contenttypes.api.ContributionUseCases
import eu.tib.orkg.prototype.contenttypes.api.CreateContributionUseCase
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.api.PaperRepresentation
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
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

const val PAPER_JSON_V2 = "application/vnd.orkg.paper.v2+json"

@RestController
@RequestMapping("/api/papers", produces = [PAPER_JSON_V2])
class PaperController(
    private val service: PaperUseCases,
    private val contributionService: ContributionUseCases
) : BaseController(), PaperRepresentationAdapter, ContributionRepresentationAdapter {
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

    @GetMapping
    fun findAll(
        @RequestParam("doi", required = false) doi: String?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<PaperRepresentation> {
        if (setOf(doi, title, visibility, createdBy).size > 2)
            throw TooManyParameters.atMostOneOf("doi", "title", "visibility", "created_by")
        return when {
            doi != null -> service.findAllByDOI(doi, pageable)
            title != null -> service.findAllByTitle(title, pageable)
            visibility != null -> service.findAllByVisibility(visibility, pageable)
            createdBy != null -> service.findAllByContributor(createdBy, pageable)
            else -> service.findAll(pageable)
        }.mapToPaperRepresentation()
    }

    @GetMapping(params = ["visibility", "research_field"])
    fun findAll(
        @RequestParam("visibility") visibility: VisibilityFilter,
        @RequestParam("research_field") researchField: ThingId,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<PaperRepresentation> =
        service.findAllByResearchFieldAndVisibility(researchField, visibility, includeSubfields, pageable)
            .mapToPaperRepresentation()

    @PostMapping(consumes = [PAPER_JSON_V2])
    fun create(
        @RequestBody @Validated request: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<PaperRepresentation> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @PostMapping("/{id}/contributions", produces = [CONTRIBUTION_JSON_V2], consumes = [CONTRIBUTION_JSON_V2])
    fun createContribution(
        @PathVariable("id") paperId: ThingId,
        @RequestBody @Validated request: CreateContributionRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ContributionRepresentation> {
        val userId = ContributorId(authenticatedUserId())
        val id = service.createContribution(request.toCreateCommand(userId, paperId))
        val location = uriComponentsBuilder
            .path("api/contributions/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @PostMapping("/{id}/publish", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun publish(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: PublishRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        service.publish(id, request.subject, request.description)
        val location = uriComponentsBuilder
            .path("api/papers/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreatePaperRequest(
        @NotBlank
        val title: String,
        @Size(min = 1, max = 1)
        @JsonProperty("research_fields")
        val researchFields: List<ThingId>,
        val identifiers: Map<String, String>?,
        @JsonProperty("publication_info")
        val publicationInfo: PublicationInfoDTO?,
        val authors: List<AuthorDTO>,
        @Size(max = 1)
        val observatories: List<ObservatoryId>,
        @Size(max = 1)
        val organizations: List<OrganizationId>,
        val contents: PaperContentsDTO?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        data class PublicationInfoDTO(
            @Size(min = 1, max = 12)
            @JsonProperty("published_month")
            val publishedMonth: Int?,
            @JsonProperty("published_year")
            val publishedYear: Long?,
            @NotBlank
            @JsonProperty("published_in")
            val publishedIn: String?,
            val url: URI?
        ) {
            fun toCreateCommand(): PublicationInfo =
                PublicationInfo(
                    publishedMonth = publishedMonth,
                    publishedYear = publishedYear,
                    publishedIn = publishedIn,
                    url = url
                )
        }

        data class PaperContentsDTO(
            val resources: Map<String, ResourceDefinitionDTO>?,
            val literals: Map<String, LiteralDefinitionDTO>?,
            val predicates: Map<String, PredicateDefinitionDTO>?,
            val lists: Map<String, ListDefinitionDTO>?,
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

        data class ResourceDefinitionDTO(
            @NotBlank
            val label: String,
            val classes: Set<ThingId>?
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.ResourceDefinition =
                CreatePaperUseCase.CreateCommand.ResourceDefinition(
                    label = label,
                    classes = classes.orEmpty()
                )
        }

        data class LiteralDefinitionDTO(
            @NotBlank
            val label: String,
            @JsonProperty("data_type")
            val dataType: String?
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.LiteralDefinition =
                CreatePaperUseCase.CreateCommand.LiteralDefinition(
                    label = label,
                    dataType = dataType ?: Literals.XSD.STRING.prefixedUri
                )
        }

        data class PredicateDefinitionDTO(
            @NotBlank
            val label: String,
            @Size(min = 1)
            val description: String?
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.PredicateDefinition =
                CreatePaperUseCase.CreateCommand.PredicateDefinition(
                    label = label,
                    description = description
                )
        }

        data class ListDefinitionDTO(
            @NotBlank
            val label: String,
            val elements: List<String>
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.ListDefinition =
                CreatePaperUseCase.CreateCommand.ListDefinition(
                    label = label,
                    elements = elements
                )
        }

        data class ContributionDTO(
            @NotBlank
            val label: String,
            val classes: Set<ThingId>?,
            @Size(min = 1)
            val statements: Map<String, List<StatementObjectDefinitionDTO>>
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.Contribution =
                CreatePaperUseCase.CreateCommand.Contribution(
                    label = label,
                    classes = classes.orEmpty(),
                    statements = statements.mapValues { it.value.map { statement -> statement.toCreateCommand() } }
                )
        }

        data class StatementObjectDefinitionDTO(
            val id: String,
            @Size(min = 1)
            val statements: Map<String, List<StatementObjectDefinitionDTO>>?
        ) {
            fun toCreateCommand(): CreatePaperUseCase.CreateCommand.StatementObjectDefinition =
                CreatePaperUseCase.CreateCommand.StatementObjectDefinition(
                    id = id,
                    statements = statements?.mapValues { it.value.map { statement -> statement.toCreateCommand() } }
                )
        }

        fun toCreateCommand(contributorId: ContributorId): CreatePaperUseCase.CreateCommand =
            CreatePaperUseCase.CreateCommand(
                contributorId = contributorId,
                title = title,
                researchFields = researchFields,
                identifiers = identifiers.orEmpty(),
                publicationInfo = publicationInfo?.toCreateCommand(),
                authors = authors.map { it.toCreateCommand() },
                observatories = observatories,
                organizations = organizations,
                contents = contents?.toCreateCommand(),
                extractionMethod = extractionMethod
            )
    }

    data class CreateContributionRequest(
        val resources: Map<String, CreatePaperRequest.ResourceDefinitionDTO>?,
        val literals: Map<String, CreatePaperRequest.LiteralDefinitionDTO>?,
        val predicates: Map<String, CreatePaperRequest.PredicateDefinitionDTO>?,
        val lists: Map<String, CreatePaperRequest.ListDefinitionDTO>?,
        val contribution: CreatePaperRequest.ContributionDTO
    ) {
        fun toCreateCommand(contributorId: ContributorId, paperId: ThingId): CreateContributionUseCase.CreateCommand =
            CreateContributionUseCase.CreateCommand(
                contributorId = contributorId,
                paperId = paperId,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                contribution = contribution.toCreateCommand()
            )
    }

    data class PublishRequest(
        @NotBlank
        val subject: String,
        @NotBlank
        val description: String
    )
}
