package org.orkg.graph.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ThingRepository {
    fun findById(id: ThingId): Optional<Thing>

    fun findAll(pageable: Pageable): Page<Thing>

    fun existsAllById(ids: Set<ThingId>): Boolean

    fun isUsedAsObject(id: ThingId): Boolean
}
