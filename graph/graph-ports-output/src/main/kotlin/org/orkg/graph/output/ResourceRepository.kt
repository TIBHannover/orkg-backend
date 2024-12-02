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
        baseClass: ThingId? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<Resource>
    fun findPaperByLabel(label: String): Optional<Resource>
    fun findAllPapersByLabel(label: String): List<Resource>
    fun findPaperById(id: ThingId): Optional<Resource>
    fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource>
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>

    // TODO: refactor or remove classIn methods
    fun findAllByClassInAndVisibility(classes: Set<ThingId>, visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource>
    fun findAllByClassInAndVisibilityAndObservatoryId(classes: Set<ThingId>, visibility: Visibility, id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllListedByClassInAndObservatoryId(classes: Set<ThingId>, id: ObservatoryId, pageable: Pageable): Page<Resource>
}
