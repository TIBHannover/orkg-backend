package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.RosettaStoneTemplateRepresentationAdapter
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val ROSETTA_STONE_TEMPLATE_JSON_V1 = "application/vnd.orkg.rosetta-stone-template.v1+json"

@RestController
@RequestMapping("/api/rosetta-stone/templates", produces = [ROSETTA_STONE_TEMPLATE_JSON_V1])
class RosettaStoneTemplateController(
    private val service: RosettaStoneTemplateUseCases
) : RosettaStoneTemplateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId
    ): RosettaStoneTemplateRepresentation =
        service.findById(id)
            .mapToRosettaStoneTemplateRepresentation()
            .orElseThrow { RosettaStoneTemplateNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        pageable: Pageable
    ): Page<RosettaStoneTemplateRepresentation> =
        service.findAll(
            searchString = string?.let { SearchString.of(string, exactMatch = exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            pageable = pageable
        ).mapToRosettaStoneTemplateRepresentation()
}
