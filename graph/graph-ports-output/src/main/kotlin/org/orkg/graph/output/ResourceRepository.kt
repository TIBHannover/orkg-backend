package org.orkg.graph.output

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResourceRepository : EntityRepository<Resource, ThingId> {
    fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource?

    // legacy methods:
    fun nextIdentity(): ThingId
    fun save(resource: Resource)
    fun deleteById(id: ThingId)
    fun deleteAll()
    fun findById(id: ThingId): Optional<Resource>
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        includeClasses: Set<ThingId> = emptySet(),
        excludeClasses: Set<ThingId> = emptySet(),
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<Resource>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Resource>
    fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndCreatedBy(`class`: ThingId, createdBy: ContributorId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabel(`class`: ThingId, labelSearchString: SearchString, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        labelSearchString: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource>
    fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource>
    fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Resource>
    fun findPaperByLabel(label: String): Optional<Resource>
    fun findAllPapersByLabel(label: String): Iterable<Resource>
    fun findAllByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findPaperById(id: ThingId): Optional<Resource>
    fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource>
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
    fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>

    fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListed(pageable: Pageable): Page<Resource>

    fun findAllByClassAndVisibility(classId: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedByClass(classId: ThingId, pageable: Pageable): Page<Resource>

    fun findAllByClassInAndVisibility(classes: Set<ThingId>, visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource>

    fun findAllByClassInAndVisibilityAndObservatoryId(classes: Set<ThingId>, visibility: Visibility, id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllListedByClassInAndObservatoryId(classes: Set<ThingId>, id: ObservatoryId, pageable: Pageable): Page<Resource>
}
