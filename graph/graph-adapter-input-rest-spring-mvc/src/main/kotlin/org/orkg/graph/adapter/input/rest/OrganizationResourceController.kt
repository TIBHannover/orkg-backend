package org.orkg.graph.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.OrganizationId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/organizations", produces = [MediaType.APPLICATION_JSON_VALUE])
class OrganizationResourceController(
    private val resourceService: ResourceUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter {
    @GetMapping("/{id}/problems")
    fun findProblemsByOrganizationId(
        @PathVariable id: OrganizationId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        resourceService.findAllProblemsByOrganizationId(id, pageable)
            .mapToResourceRepresentation(capabilities)
}
