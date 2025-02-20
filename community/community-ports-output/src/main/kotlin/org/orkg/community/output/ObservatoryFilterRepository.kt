package org.orkg.community.output

import org.orkg.common.ObservatoryId
import org.orkg.community.domain.ObservatoryFilter
import org.orkg.community.domain.ObservatoryFilterId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ObservatoryFilterRepository {
    fun save(observatoryFilter: ObservatoryFilter)

    fun findById(id: ObservatoryFilterId): Optional<ObservatoryFilter>

    fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<ObservatoryFilter>

    fun deleteById(id: ObservatoryFilterId)

    fun nextIdentity(): ObservatoryFilterId

    fun deleteAll()
}
