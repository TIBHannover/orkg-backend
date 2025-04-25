package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface ThingUseCases : RetrieveThingUseCase

interface RetrieveThingUseCase {
    fun existsById(id: ThingId): Boolean

    fun findById(id: ThingId): Optional<Thing>

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
    ): Page<Thing>
}
