package org.orkg.graph.output

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

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

    fun count(
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
    ): Long

    fun findPaperByLabel(label: String): Optional<Resource>

    fun findAllPapersByLabel(label: String): List<Resource>

    fun findPaperById(id: ThingId): Optional<Resource>

    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
}
