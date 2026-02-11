package org.orkg.graph.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.community.domain.InvalidFilterConfig
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

@RestController
@RequestMapping("/api/observatories", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObservatoryResourceController(
    private val resourceService: ResourceUseCases,
    private val objectMapper: ObjectMapper,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter {
    @GetMapping("/{id}/papers")
    fun findAllPapersByObservatoryId(
        @PathVariable id: ObservatoryId,
        @RequestParam("filter_config", required = false) filterConfig: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> =
        resourceService.findAllPapersByObservatoryIdAndFilters(
            observatoryId = id,
            filters = objectMapper.parseFilterConfig(filterConfig),
            visibility = visibility ?: VisibilityFilter.ALL_LISTED,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
}

internal fun ObjectMapper.parseFilterConfig(filterConfig: String?): List<SearchFilter> =
    filterConfig?.let {
        try {
            readValue(filterConfig, object : TypeReference<List<SearchFilter>>() {})
        } catch (_: Exception) {
            throw InvalidFilterConfig()
        }
    }.orEmpty()
