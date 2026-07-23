package org.orkg.graph.adapter.input.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.mapping.PathRepresentationAdapter
import org.orkg.graph.domain.PathDirection
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.PathUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/things/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
class PathController(
    private val pathUseCases: PathUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : PathRepresentationAdapter {
    @GetMapping("/paths")
    fun findAllByRootId(
        @PathVariable id: ThingId,
        @Valid @Min(1) @Max(10) @RequestParam("min_hops", required = false) minHops: Int? = null,
        @Valid @Min(1) @Max(10) @RequestParam("max_hops", required = false) maxHops: Int? = null,
        @RequestParam("deny_classes", required = false) denyClasses: Set<ThingId>?,
        @RequestParam("allow_classes", required = false) allowClasses: Set<ThingId>?,
        @RequestParam("termination_classes", required = false) terminationClasses: Set<ThingId>?,
        @RequestParam("direction", required = false) direction: PathDirection = PathDirection.OUTGOING,
        @RequestParam("include_root", required = false) includeRoot: Boolean = true,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<PathRepresentation> =
        pathUseCases.findAllByRootId(
            id = id,
            pageable = pageable,
            minHops = minHops,
            maxHops = maxHops,
            denyClasses = denyClasses.orEmpty(),
            allowClasses = allowClasses.orEmpty(),
            terminationClasses = terminationClasses.orEmpty(),
            pathDirection = direction,
            includeRoot = includeRoot,
        ).mapToPathRepresentation(capabilities)

    @GetMapping("/inverse-paths")
    fun findAllByRootIdInverse(
        @PathVariable id: ThingId,
        @Valid @Min(1) @Max(10) @RequestParam("min_hops", required = false) minHops: Int? = null,
        @Valid @Min(1) @Max(10) @RequestParam("max_hops", required = false) maxHops: Int? = null,
        @RequestParam("deny_classes", required = false) denyClasses: Set<ThingId>?,
        @RequestParam("allow_classes", required = false) allowClasses: Set<ThingId>?,
        @RequestParam("termination_classes", required = false) terminationClasses: Set<ThingId>?,
        @RequestParam("direction", required = false) direction: PathDirection = PathDirection.OUTGOING,
        @RequestParam("include_root", required = false) includeRoot: Boolean = true,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<List<PathRepresentation>> =
        pathUseCases.findAllByRootIdInverse(
            id = id,
            pageable = pageable,
            minHops = minHops,
            maxHops = maxHops,
            denyClasses = denyClasses.orEmpty(),
            allowClasses = allowClasses.orEmpty(),
            terminationClasses = terminationClasses.orEmpty(),
            pathDirection = direction,
            includeRoot = includeRoot,
        ).mapToInversePathRepresentation(capabilities)
}
