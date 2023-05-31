package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.ResourceRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
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
    fun findPapersByObservatoryId(@PathVariable id: ObservatoryId): Iterable<ResourceRepresentation> =
        resourceService.findPapersByObservatoryId(id).mapToResourceRepresentation()

    @GetMapping("{id}/comparisons")
    fun findComparisonsByObservatoryId(@PathVariable id: ObservatoryId): Iterable<ResourceRepresentation> =
        resourceService.findComparisonsByObservatoryId(id).mapToResourceRepresentation()

    @GetMapping("{id}/problems")
    fun findProblemsByObservatoryId(@PathVariable id: ObservatoryId, pageable: Pageable): Page<ResourceRepresentation> =
        resourceService.findProblemsByObservatoryId(id, pageable).mapToResourceRepresentation()

    @GetMapping("{id}/class")
    fun findProblemsByObservatoryId(
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
