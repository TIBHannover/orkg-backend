package org.orkg.contenttypes.input

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveComparisonUseCase {
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
        includeSubfields: Boolean = false
    ): Page<Comparison>
    fun findById(id: ThingId): Optional<Comparison>
    fun findAllByDOI(doi: String, pageable: Pageable): Page<Comparison>
    fun findAllByTitle(title: String, pageable: Pageable): Page<Comparison>
    fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Comparison>
    fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Comparison>
    fun findRelatedResourceById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource>
    fun findAllRelatedResources(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource>
    fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure>
    fun findAllRelatedFigures(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure>
    /* An unpublished comparison is a comparison that does not have a DOI and is not a draft comparison (ComparisonDraft) */
    fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Comparison>
    fun findAllByResearchFieldAndVisibility(
        researchFieldId: ThingId,
        visibility: VisibilityFilter,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Comparison>
}
