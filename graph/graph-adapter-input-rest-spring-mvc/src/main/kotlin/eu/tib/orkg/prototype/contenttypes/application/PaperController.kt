package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.PaperRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private const val PAPER_JSON_V2 = "application/vnd.orkg.paper.v2+json"

@RestController
@RequestMapping("/api/papers/", produces = [MediaType.APPLICATION_JSON_VALUE])
class PaperController(
    private val service: PaperUseCases
) : BaseController(), PaperRepresentationAdapter {
    @GetMapping("/{id}", produces = [PAPER_JSON_V2])
    fun findById(
        @PathVariable id: ThingId
    ): PaperRepresentation = service.findById(id).toPaperRepresentation()

    @GetMapping("/{id}/contributors", produces = [PAPER_JSON_V2])
    fun findAllContributorsByPaperId(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ContributorId> = service.findAllContributorsByPaperId(id, pageable)

    @GetMapping("/", produces = [PAPER_JSON_V2])
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
}
