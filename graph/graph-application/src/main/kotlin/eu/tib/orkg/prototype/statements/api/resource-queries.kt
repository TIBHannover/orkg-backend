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
    fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource?

    // Legacy methods:
    fun findAll(pageable: Pageable): Page<Resource>
    fun findAllByClass(pageable: Pageable, id: ThingId): Page<Resource>
    fun findAllByClassAndCreatedBy(pageable: Pageable, id: ThingId, createdBy: ContributorId): Page<Resource>
    fun findAllByClassAndLabel(id: ThingId, label: SearchString, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelAndCreatedBy(
        id: ThingId,
        label: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource>
    fun findAllByLabel(label: SearchString, pageable: Pageable): Page<Resource>
    fun findAllByTitle(title: String?): Iterable<Resource>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Resource>
    fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource>
    fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        label: SearchString,
        pageable: Pageable
    ): Page<Resource>
    fun findByDOI(doi: String): Optional<Resource>
    fun findById(id: ThingId): Optional<Resource>
    fun findByTitle(title: String): Optional<Resource>
    fun findAllComparisonsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>
    fun findAllPapersByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllByClassInAndVisibilityAndObservatoryId(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        id: ObservatoryId,
        pageable: Pageable
    ): Page<Resource>
    fun findAllByClassInAndVisibility(
        classes: Set<ThingId>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource>
    fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
    fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
    fun hasStatements(id: ThingId): Boolean
}
