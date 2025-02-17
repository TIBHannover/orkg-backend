package org.orkg.graph.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ThingRepository {
    fun findById(id: ThingId): Optional<Thing>
    fun findAll(pageable: Pageable): Page<Thing>
    fun existsAllById(ids: Set<ThingId>): Boolean
    fun isUsedAsObject(id: ThingId): Boolean
}
