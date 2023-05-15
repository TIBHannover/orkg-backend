package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResourceUseCase {
    fun exists(id: ThingId): Boolean
    fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): ResourceRepresentation?

    // TODO: Needed by problem service. May need better solution.
    fun map(action: IterableResourcesGenerator): Iterable<ResourceRepresentation>
    fun map(action: PagedResourcesGenerator): Page<ResourceRepresentation>
    fun map(action: ResourceGenerator): ResourceRepresentation

    // Legacy methods:
    fun findAll(pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByClass(pageable: Pageable, id: ThingId): Page<ResourceRepresentation>
    fun findAllByClassAndCreatedBy(pageable: Pageable, id: ThingId, createdBy: ContributorId): Page<ResourceRepresentation>
    fun findAllByClassAndLabel(id: ThingId, label: SearchString, pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByClassAndLabelAndCreatedBy(
        id: ThingId,
        label: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<ResourceRepresentation>
    fun findAllByLabel(label: SearchString, pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByTitle(title: String?): Iterable<ResourceRepresentation>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<ResourceRepresentation>
    fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<ResourceRepresentation>
    fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: SearchString,
        pageable: Pageable
    ): Page<ResourceRepresentation>
    fun findByDOI(doi: String): Optional<ResourceRepresentation>
    fun findById(id: ThingId): Optional<ResourceRepresentation>
    fun findByTitle(title: String): Optional<ResourceRepresentation>
    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation>
    fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>
    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<ResourceRepresentation>
    fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<ResourceRepresentation>
    fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<ResourceRepresentation>
    fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<ResourceRepresentation>
    fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation>
    fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<ResourceRepresentation>
    fun hasStatements(id: ThingId): Boolean
}

fun interface ResourceGenerator {
    fun generate(): Resource
}

fun interface IterableResourcesGenerator {
    fun generate(): Iterable<Resource>
}

fun interface PagedResourcesGenerator {
    fun generate(): Page<Resource>
}
