package org.orkg.graph.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
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

    /** Find any resource by DOI that has one of the publishable classes. */
    fun findByDOI(doi: String): Optional<Resource>

    /** Find a paper resource by DOI. */
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
