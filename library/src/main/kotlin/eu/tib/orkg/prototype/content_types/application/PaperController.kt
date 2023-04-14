package eu.tib.orkg.prototype.content_types.application

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.content_types.api.CreatePaperUseCase
import eu.tib.orkg.prototype.content_types.api.CreatePaperUseCase.*
import eu.tib.orkg.prototype.content_types.api.CreatePaperUseCase.CreateCommand.*
import eu.tib.orkg.prototype.content_types.api.PaperRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperUseCases
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/content-types/paper/")
class PaperController(
    private val service: PaperUseCases
): BaseController() {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): PaperRepresentation = service.findById(id)

    // TODO: featured, listed, unlisted, date range?
    @GetMapping("/")
    fun findAll(
        @RequestParam("doi", required = false) doi: String?,
        @RequestParam("title", required = false) title: String?,
        pageable: Pageable
    ): Page<PaperRepresentation> {
        if (doi != null && title != null)
            throw TooManyParameters.atMostOneOf("doi", "title")
        return when {
            doi != null -> service.findAllByDOI(doi, pageable)
            title != null -> service.findAllByTitle(title, pageable)
            else -> service.findAll(pageable)
        }
    }

    // TODO: merge option? use PATCH instead -> but no merge via title?
    @PostMapping("/")
    fun create(
        @RequestBody request: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<PaperRepresentation> {
        val user = authenticatedUserId()
        val id = service.create(request.toCreateCommand(ContributorId(user)))
        val location = uriComponentsBuilder
            .path("api/content-types/paper/")
            .buildAndExpand(id)
            .toUri()
        return ResponseEntity.created(location).body(service.findById(id))
    }

    data class CreatePaperRequest(
        @NotBlank
        val title: String,
        @Size(min = 1)
        val researchFields: List<ThingId>,
        val identifiers: Map<String, @NotBlank String>?,
        val publicationInfo: PublicationInfoRequest?,
        @Size(min = 1)
        val authors: List<AuthorRequest>,
        @Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @Size(max = 1)
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?
    ) {
        data class PublicationInfoRequest(
            @Min(1)
            @Max(12)
            val publishedMonth: Int?,
            @Min(0)
            val publishedYear: Long?,
            val publishedIn: String?,
            val url: String?
        ) {
            fun toCreateCommand() = PublicationInfo(
                publishedMonth = publishedMonth,
                publishedYear = publishedYear,
                publishedIn = publishedIn,
                url = url
            )
        }

        data class AuthorRequest(
            @NotBlank
            val name: String?,
            val identifiers: Map<String, @NotBlank String>?,
            val homepage: String?
        ) {
            fun toCreateCommand() = Author(
                name = name,
                identifiers = identifiers,
                homepage = homepage
            )
        }

        fun toCreateCommand(contributorId: ContributorId) = CreateCommand(
            contributorId = contributorId,
            title = title,
            researchFields = researchFields,
            identifiers = identifiers,
            publicationInfo = publicationInfo?.toCreateCommand(),
            authors = authors.map { it.toCreateCommand() },
            observatories = observatories.orEmpty(),
            organizations = organizations.orEmpty(),
            extractionMethod = extractionMethod ?: ExtractionMethod.UNKNOWN
        )
    }
}
