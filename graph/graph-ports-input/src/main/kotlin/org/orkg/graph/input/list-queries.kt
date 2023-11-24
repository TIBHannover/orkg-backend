package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.List
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveListUseCase {
    fun findById(id: ThingId): Optional<List>
    fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing>
    fun exists(id: ThingId): Boolean
}
