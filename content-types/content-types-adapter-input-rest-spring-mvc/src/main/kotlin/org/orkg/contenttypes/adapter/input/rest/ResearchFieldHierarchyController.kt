package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ResearchFieldHierarchyUseCases
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResearchFieldHierarchyEntryRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResearchFieldWithChildCountRepresentationAdapter
import org.orkg.graph.adapter.input.rest.ResearchFieldHierarchyEntryRepresentation
import org.orkg.graph.adapter.input.rest.ResearchFieldWithChildCountRepresentation
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/research-fields", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResearchFieldHierarchyController(
    private val service: ResearchFieldHierarchyUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService
) : ResearchFieldHierarchyEntryRepresentationAdapter,
    ResearchFieldWithChildCountRepresentationAdapter {

    @GetMapping("/{id}/children")
    fun findChildren(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResearchFieldWithChildCountRepresentation> =
        service.findChildren(id, pageable)
            .mapToResearchFieldWithChildCountRepresentation(capabilities)

    @GetMapping("/{id}/parents")
    fun findParent(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        service.findParents(id, pageable)
            .mapToResourceRepresentation(capabilities)

    @GetMapping("/{id}/roots")
    fun findRoots(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        service.findRoots(id, pageable)
            .mapToResourceRepresentation(capabilities)

    @GetMapping("/roots")
    fun findAllRoots(
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        service.findAllRoots(pageable)
            .mapToResourceRepresentation(capabilities)

    @GetMapping("/{id}/hierarchy")
    fun findResearchFieldHierarchy(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResearchFieldHierarchyEntryRepresentation> =
        service.findResearchFieldHierarchy(id, pageable)
            .mapToResearchFieldHierarchyEntryRepresentation(capabilities)
}
