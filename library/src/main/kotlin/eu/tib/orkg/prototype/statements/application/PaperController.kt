package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.PaperResourceWithPathRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod.UNKNOWN
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.PaperService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/papers/", produces = [MediaType.APPLICATION_JSON_VALUE])
class PaperController(
    private val service: PaperService,
) : BaseController() {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @RequestBody paper: CreatePaperRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @RequestParam("mergeIfExists", required = false, defaultValue = "false") mergeIfExists: Boolean
    ): ResponseEntity<ResourceRepresentation> {
        val resource = service.addPaperContent(paper, mergeIfExists, authenticatedUserId())
        val location = uriComponentsBuilder
            .path("api/resources/")
            .buildAndExpand(resource.id)
            .toUri()
        return ResponseEntity.created(location).body(resource)
    }

    @GetMapping("/")
    fun findPaperResourcesRelatedTo(
        @RequestParam("linkedTo", required = true) id: ThingId,
        pageable: Pageable
    ): Page<PaperResourceWithPathRepresentation> =
        service.findPapersRelatedToResource(id, pageable)
}

/**
 * Main entry point, basic skeleton of a paper
 */
data class CreatePaperRequest(
    val predicates: List<HashMap<String, String>>?,
    val paper: Paper
)

/**
 * Concrete paper holder class
 * contains meta-information of papers
 * and helper methods
 */
data class Paper(
    val title: String,
    val doi: String?,
    val authors: List<Author>?,
    val publicationMonth: Int?,
    val publicationYear: Int?,
    val publishedIn: String?,
    val url: String?,
    val researchField: ThingId,
    val contributions: List<NamedObject>?,
    val extractionMethod: ExtractionMethod = UNKNOWN
) {
    /**
     * Check if the paper has a published in venue
     */
    fun hasPublishedIn(): Boolean =
        publishedIn?.isNotEmpty() == true

    /**
     * Check if the paper has a DOI
     */
    fun hasDOI(): Boolean =
        doi?.isNotEmpty() == true

    /**
     * Check if the paper has a URL
     */
    fun hasUrl(): Boolean =
        url?.isNotEmpty() == true

    /**
     * Check if the paper has contributions
     */
    fun hasContributions() =
        !contributions.isNullOrEmpty()

    /**
     * Check if the paper has authors
     */
    fun hasAuthors() =
        !authors.isNullOrEmpty()

    /**
     * Check if the paper has a publication month value
     */
    fun hasPublicationMonth() =
        publicationMonth != null

    /**
     * Check if the paper has a publication year value
     */
    fun hasPublicationYear() =
        publicationYear != null
}

/**
 * Author class container
 */
data class Author(
    val id: ThingId?,
    val label: String?,
    val orcid: String?
) {
    /**
     * Check if the author has a name (label)
     * and an ORCID at the same time
     */
    fun hasNameAndOrcid() =
        label != null && orcid != null

    /**
     * Check if the author is an existing resource
     * i.e., the id of the author is not null
     */
    fun hasId() = id != null
}
