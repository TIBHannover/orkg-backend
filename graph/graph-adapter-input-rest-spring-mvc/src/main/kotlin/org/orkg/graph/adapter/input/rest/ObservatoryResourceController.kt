package org.orkg.graph.adapter.input.rest

import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.TemplateRepository
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
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter {

    @GetMapping("{id}/papers")
    fun findAllPapersByObservatoryId(
        @PathVariable id: ObservatoryId,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        resourceService.findAllPapersByObservatoryId(id, pageable).mapToResourceRepresentation()

    @GetMapping("{id}/comparisons")
    fun findAllComparisonsByObservatoryId(
        @PathVariable id: ObservatoryId,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        resourceService.findAllComparisonsByObservatoryId(id, pageable).mapToResourceRepresentation()

    @GetMapping("{id}/problems")
    fun findAllProblemsByObservatoryId(
        @PathVariable id: ObservatoryId,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        resourceService.findAllProblemsByObservatoryId(id, pageable).mapToResourceRepresentation()

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
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        resourceService.findAllByClassInAndVisibilityAndObservatoryId(
            classes = classes,
            visibility = visibility ?: visibilityFilterFromFlags(featured, unlisted),
            id = id,
            pageable = pageable
        ).mapToResourceRepresentation()
}
