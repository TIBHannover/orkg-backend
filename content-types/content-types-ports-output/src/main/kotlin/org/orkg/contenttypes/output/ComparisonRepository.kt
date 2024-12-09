package org.orkg.contenttypes.output

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ComparisonRepository {
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        doi: String? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        published: Boolean? = null,
        sustainableDevelopmentGoal: ThingId? = null,
        researchProblem: ThingId? = null
    ): Page<Resource>
    // This method could be moved to a separate ComparisonPublishedRepository
    fun findVersionHistoryForPublishedComparison(id: ThingId): VersionInfo
    fun findAllDOIsRelatedToComparison(id: ThingId): Iterable<String>
    fun findAllCurrentAndListedAndUnpublishedComparisons(pageable: Pageable): Page<Resource>
}
