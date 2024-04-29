package org.orkg.community.input

import org.orkg.community.domain.ObservatoryFilter
import org.orkg.community.domain.ObservatoryFilterId
import java.util.*
import org.orkg.common.ObservatoryId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveObservatoryFilterUseCase {
    fun findById(id: ObservatoryFilterId): Optional<ObservatoryFilter>
    fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<ObservatoryFilter>
}
