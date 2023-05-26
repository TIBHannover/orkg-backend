package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ResourceRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
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
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : ResourceRepresentationAdapter {

    @GetMapping("{id}/comparisons")
    fun findComparisonsByOrganizationId(@PathVariable id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> =
        resourceService.findComparisonsByOrganizationId(id, pageable).mapToResourceRepresentation()

    @GetMapping("{id}/problems")
    fun findProblemsByOrganizationId(@PathVariable id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation> =
        resourceService.findProblemsByOrganizationId(id, pageable).mapToResourceRepresentation()
}
