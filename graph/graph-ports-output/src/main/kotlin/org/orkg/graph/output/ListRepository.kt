package org.orkg.graph.output

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.List
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ListRepository {
    fun save(list: List, contributorId: ContributorId)
    fun findById(id: ThingId): Optional<List>
    fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing>
    fun nextIdentity(): ThingId
    fun exists(id: ThingId): Boolean
    fun delete(id: ThingId)
}
