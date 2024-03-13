package org.orkg.contenttypes.output

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
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
        sustainableDevelopmentGoal: ThingId? = null
    ): Page<Resource>
    fun findVersionHistory(id: ThingId): List<HeadVersion>
    fun findAllDOIsRelatedToComparison(id: ThingId): Iterable<String>
    fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Resource>

    // legacy methods:

    // always returns all head and all previous versions
    @Deprecated(message = "To be removed", replaceWith = ReplaceWith("findAll"))
    fun findAllListedComparisonsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    // always returns all head and all previous versions
    @Deprecated(message = "To be removed", replaceWith = ReplaceWith("findAll"))
    fun findAllComparisonsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>
}
