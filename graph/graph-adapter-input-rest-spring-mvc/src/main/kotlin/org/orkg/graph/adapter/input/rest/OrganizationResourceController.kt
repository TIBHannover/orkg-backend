package org.orkg.graph.adapter.input.rest

import org.orkg.common.OrganizationId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
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
@RequestMapping("/api/organizations/", produces = [MediaType.APPLICATION_JSON_VALUE])
class OrganizationResourceController(
    private val resourceService: ResourceUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter {

    @GetMapping("{id}/comparisons")
    fun findComparisonsByOrganizationId(@PathVariable id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> =
        resourceService.findAll(
            includeClasses = setOf(Classes.comparison),
            organizationId = id,
            pageable = pageable
        ).mapToResourceRepresentation()

    @GetMapping("{id}/problems")
    fun findProblemsByOrganizationId(@PathVariable id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> =
        resourceService.findAllProblemsByOrganizationId(id, pageable).mapToResourceRepresentation()
}
