package org.orkg.graph.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ThingRepository {
    // legacy methods:
    fun findByThingId(id: ThingId): Optional<Thing>
    fun findAll(pageable: Pageable): Page<Thing>
    fun existsAll(ids: Set<ThingId>): Boolean
}
