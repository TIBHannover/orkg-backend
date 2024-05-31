package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.community.domain.InvalidFilterConfig
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/observatories/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObservatoryResourceController(
    private val resourceService: ResourceUseCases,
    private val objectMapper: ObjectMapper,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter {

    @GetMapping("{id}/papers")
    fun findAllPapersByObservatoryId(
        @PathVariable id: ObservatoryId,
        @RequestParam("filter_config", required = false) filterConfig: String?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        resourceService.findAllPapersByObservatoryIdAndFilters(
            observatoryId = id,
            filters = objectMapper.parseFilterConfig(filterConfig),
            visibility = visibility ?: VisibilityFilter.ALL_LISTED,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)

    @GetMapping("{id}/problems")
    fun findAllProblemsByObservatoryId(
        @PathVariable id: ObservatoryId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        resourceService.findAllProblemsByObservatoryId(id, pageable)
            .mapToResourceRepresentation(capabilities)

    @GetMapping("{id}/class")
    fun findAllResourcesByClassInAndVisibilityAndObservatoryId(
        @PathVariable id: ObservatoryId,
        @RequestParam(value = "classes") classes: Set<ThingId>,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        resourceService.findAllByClassInAndVisibilityAndObservatoryId(
            classes = classes,
            visibility = visibility ?: visibilityFilterFromFlags(featured, unlisted),
            id = id,
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
