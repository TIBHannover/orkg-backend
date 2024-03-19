package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.RosettaTemplateRepresentationAdapter
import org.orkg.contenttypes.domain.RosettaTemplateNotFound
import org.orkg.contenttypes.input.RosettaTemplateUseCases
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val ROSETTA_TEMPLATE_JSON_V1 = "application/vnd.orkg.rosetta-template.v1+json"

@RestController
@RequestMapping("/api/rosetta/templates", produces = [ROSETTA_TEMPLATE_JSON_V1])
class RosettaTemplateController(
    private val service: RosettaTemplateUseCases
) : RosettaTemplateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): RosettaTemplateRepresentation =
        service.findById(id)
            .mapToTemplateRepresentation()
            .orElseThrow { RosettaTemplateNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<RosettaTemplateRepresentation> =
        service.findAll(
            searchString = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            pageable = pageable
        ).mapToTemplateRepresentation()
}
