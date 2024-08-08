package org.orkg.graph.input

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResourceUseCase {
    fun exists(id: ThingId): Boolean
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        includeClasses: Set<ThingId> = emptySet(),
        excludeClasses: Set<ThingId> = emptySet(),
        baseClass: ThingId? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<Resource>
    // Legacy methods:
    fun findAllPapersByTitle(title: String?): Iterable<Resource>

    fun findByDOI(doi: String, classes: Set<ThingId>): Optional<Resource>

    /** Find a paper resource by DOI. */
    fun findById(id: ThingId): Optional<Resource>
    fun findPaperByTitle(title: String): Optional<Resource>
    fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor>
    fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource>
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
    fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
}
