package org.orkg.community.adapter.output.jpa.internal

import java.util.*
import org.orkg.community.domain.ObservatoryFilterId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresObservatoryFilterRepository : JpaRepository<ObservatoryFilterEntity, UUID> {
    fun findById(id: ObservatoryFilterId): Optional<ObservatoryFilterEntity>
    fun findAllByObservatoryId(observatoryId: UUID, pageable: Pageable): Page<ObservatoryFilterEntity>
}
