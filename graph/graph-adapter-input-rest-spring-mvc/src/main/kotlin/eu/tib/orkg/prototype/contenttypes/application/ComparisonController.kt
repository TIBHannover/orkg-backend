package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.ComparisonRelatedFigureRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.ComparisonRelatedResourceRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.ComparisonRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.api.ComparisonUseCases
import eu.tib.orkg.prototype.community.domain.model.ContributorId
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

const val COMPARISON_JSON_V2 = "application/vnd.orkg.comparison.v2+json"

@RestController
@RequestMapping("/api/comparisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class ComparisonController(
    private val service: ComparisonUseCases
) : BaseController(), ComparisonRepresentationAdapter, ComparisonRelatedResourceRepresentationAdapter,
    ComparisonRelatedFigureRepresentationAdapter {

    @GetMapping("/{id}", produces = [COMPARISON_JSON_V2])
    fun findById(
        @PathVariable id: ThingId
    ): ComparisonRepresentation =
        service.findById(id)
            .mapToComparisonRepresentation()
            .orElseThrow { ComparisonNotFound(id) }

    @GetMapping(produces = [COMPARISON_JSON_V2])
    fun findAll(
        @RequestParam("doi", required = false) doi: String?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<ComparisonRepresentation> {
        if (setOf(doi, title, visibility, createdBy).size > 2)
            throw TooManyParameters.atMostOneOf("doi", "title", "visibility", "created_by")
        return when {
            doi != null -> service.findAllByDOI(doi, pageable)
            title != null -> service.findAllByTitle(title, pageable)
            visibility != null -> service.findAllByVisibility(visibility, pageable)
            createdBy != null -> service.findAllByContributor(createdBy, pageable)
            else -> service.findAll(pageable)
        }.mapToComparisonRepresentation()
    }

    @GetMapping("/{id}/related-resources/{resourceId}", produces = [COMPARISON_JSON_V2])
    fun findRelatedResourceById(
        @PathVariable("id") id: ThingId,
        @PathVariable("resourceId") resourceId: ThingId
    ): ComparisonRelatedResourceRepresentation =
        service.findRelatedResourceById(id, resourceId)
            .mapToComparisonRelatedResourceRepresentation()
            .orElseThrow { ComparisonRelatedResourceNotFound(resourceId) }

    @GetMapping("/{id}/related-resources", produces = [COMPARISON_JSON_V2])
    fun findAllRelatedResources(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ComparisonRelatedResourceRepresentation> =
        service.findAllRelatedResources(id, pageable)
            .mapToComparisonRelatedResourceRepresentation()

    @GetMapping("/{id}/related-figures/{figureId}", produces = [COMPARISON_JSON_V2])
    fun findRelatedFigureById(
        @PathVariable("id") id: ThingId,
        @PathVariable("figureId") figureId: ThingId
    ): ComparisonRelatedFigureRepresentation =
        service.findRelatedFigureById(id, figureId)
            .mapToComparisonRelatedFigureRepresentation()
            .orElseThrow { ComparisonRelatedFigureNotFound(figureId) }
    
    @GetMapping("/{id}/related-figures", produces = [COMPARISON_JSON_V2])
    fun findAllRelatedFigures(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ComparisonRelatedFigureRepresentation> =
        service.findAllRelatedFigures(id, pageable)
            .mapToComparisonRelatedFigureRepresentation()
}
