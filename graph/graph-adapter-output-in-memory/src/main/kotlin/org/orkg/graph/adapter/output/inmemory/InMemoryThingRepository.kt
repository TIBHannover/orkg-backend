package org.orkg.graph.adapter.output.inmemory

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryThingRepository(private val inMemoryGraph: InMemoryGraph) : ThingRepository {

    override fun findByThingId(id: ThingId): Optional<Thing> = inMemoryGraph.findById(id)

    override fun findAll(pageable: Pageable): Page<Thing> = TODO("Not yet implemented")

    override fun existsAll(ids: Set<ThingId>): Boolean = inMemoryGraph.findAll().map(Thing::id).containsAll(ids)
}
