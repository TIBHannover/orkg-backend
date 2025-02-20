package org.orkg.community.adapter.output.jpa.internal

import org.orkg.community.domain.ObservatoryFilterId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PostgresObservatoryFilterRepository : JpaRepository<ObservatoryFilterEntity, UUID> {
    fun findById(id: ObservatoryFilterId): Optional<ObservatoryFilterEntity>

    fun findAllByObservatoryId(observatoryId: UUID, pageable: Pageable): Page<ObservatoryFilterEntity>
}
