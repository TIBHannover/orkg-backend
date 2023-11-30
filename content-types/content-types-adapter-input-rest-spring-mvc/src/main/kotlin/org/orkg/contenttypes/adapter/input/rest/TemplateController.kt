package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.TemplateRepresentationAdapter
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val TEMPLATE_JSON_V1 = "application/vnd.orkg.template.v1+json"

@RestController
@RequestMapping("/api/templates", produces = [TEMPLATE_JSON_V1])
class TemplateController(
    private val service: TemplateUseCases
) : BaseController(), TemplateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): TemplateRepresentation =
        service.findById(id)
            .mapToTemplateRepresentation()
            .orElseThrow { TemplateNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("research_field", required = false) researchField: ThingId?,
        @RequestParam("research_problem", required = false) researchProblem: ThingId?,
        @RequestParam("target_class", required = false) targetClass: ThingId?,
        pageable: Pageable
    ): Page<TemplateRepresentation> =
        service.findAll(
            searchString = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            researchField = researchField,
            researchProblem = researchProblem,
            targetClass = targetClass,
            pageable = pageable
        ).mapToTemplateRepresentation()
}
