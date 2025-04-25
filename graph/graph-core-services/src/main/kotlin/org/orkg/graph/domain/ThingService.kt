package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.input.ThingUseCases
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j(readOnly = true)
class ThingService(
    private val repository: ThingRepository,
) : ThingUseCases {
    override fun existsById(id: ThingId): Boolean = repository.findById(id).isPresent

    override fun findById(id: ThingId): Optional<Thing> = repository.findById(id)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
    ): Page<Thing> =
        repository.findAll(
            pageable = pageable,
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            includeClasses = includeClasses,
            excludeClasses = excludeClasses,
            observatoryId = observatoryId,
            organizationId = organizationId
        )
}
