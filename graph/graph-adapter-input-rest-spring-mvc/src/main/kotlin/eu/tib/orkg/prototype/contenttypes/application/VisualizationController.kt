package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.VisualizationRepresentationAdapter
import eu.tib.orkg.prototype.contenttypes.api.VisualizationUseCases
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

const val VISUALIZATION_JSON_V2 = "application/vnd.orkg.visualization.v2+json"

@RestController
@RequestMapping("/api/visualizations", produces = [MediaType.APPLICATION_JSON_VALUE])
class VisualizationController(
    private val service: VisualizationUseCases
) : BaseController(), VisualizationRepresentationAdapter {

    @GetMapping("/{id}", produces = [VISUALIZATION_JSON_V2])
    fun findById(
        @PathVariable id: ThingId
    ): VisualizationRepresentation =
        service.findById(id)
            .mapToVisualizationRepresentation()
            .orElseThrow { VisualizationNotFound(id) }

    @GetMapping(produces = [VISUALIZATION_JSON_V2])
    fun findAll(
        @RequestParam("title", required = false) title: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<VisualizationRepresentation> {
        if (setOf(title, visibility, createdBy).size > 2)
            throw TooManyParameters.atMostOneOf("title", "visibility", "created_by")
        return when {
            title != null -> service.findAllByTitle(title, pageable)
            visibility != null -> service.findAllByVisibility(visibility, pageable)
            createdBy != null -> service.findAllByContributor(createdBy, pageable)
            else -> service.findAll(pageable)
        }.mapToVisualizationRepresentation()
    }
}
