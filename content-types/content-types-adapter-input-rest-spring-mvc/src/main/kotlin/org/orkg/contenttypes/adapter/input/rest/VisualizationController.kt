package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.contenttypes.adapter.input.rest.mapping.VisualizationRepresentationAdapter
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.domain.VisibilityFilter
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

    @GetMapping(params = ["visibility", "research_field"], produces = [VISUALIZATION_JSON_V2])
    fun findAll(
        @RequestParam("visibility") visibility: VisibilityFilter,
        @RequestParam("research_field") researchField: ThingId,
        @RequestParam("include_subfields", required = false) includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<VisualizationRepresentation> =
        service.findAllByResearchFieldAndVisibility(researchField, visibility, includeSubfields, pageable)
            .mapToVisualizationRepresentation()
}
