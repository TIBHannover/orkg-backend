package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.ResearchFieldWithChildCountRepresentationAdapter
import eu.tib.orkg.prototype.statements.ResearchFieldHierarchyEntryRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ResearchFieldWithChildCountRepresentation
import eu.tib.orkg.prototype.statements.api.ResearchFieldHierarchyEntryRepresentation
import eu.tib.orkg.prototype.statements.api.ResearchFieldHierarchyUseCases
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
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
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : BaseController(), ResearchFieldHierarchyEntryRepresentationAdapter, ResearchFieldWithChildCountRepresentationAdapter {

    @GetMapping("/{id}/children")
    fun findChildren(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ResearchFieldWithChildCountRepresentation> =
        service.findChildren(id, pageable).mapToResearchFieldWithChildCountRepresentation()

    @GetMapping("/{id}/parents")
    fun findParent(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        service.findParents(id, pageable).mapToResourceRepresentation()

    @GetMapping("/{id}/roots")
    fun findRoots(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        service.findRoots(id, pageable).mapToResourceRepresentation()

    @GetMapping("/roots")
    fun findAllRoots(
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        service.findAllRoots(pageable).mapToResourceRepresentation()

    @GetMapping("/{id}/hierarchy")
    fun findResearchFieldHierarchy(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ResearchFieldHierarchyEntryRepresentation> =
        service.findResearchFieldHierarchy(id, pageable).mapToResearchFieldHierarchyEntryRepresentation()
}
