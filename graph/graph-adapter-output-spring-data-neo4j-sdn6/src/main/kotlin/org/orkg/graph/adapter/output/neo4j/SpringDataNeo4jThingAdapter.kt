package org.orkg.graph.adapter.output.neo4j

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThing
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThingRepository
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

const val THING_ID_TO_THING_CACHE = "thing-id-to-thing"

@Component
@CacheConfig(cacheNames = [THING_ID_TO_THING_CACHE])
class SpringDataNeo4jThingAdapter(
    private val neo4jRepository: Neo4jThingRepository
) : ThingRepository {
    @Cacheable(key = "#id", cacheNames = [THING_ID_TO_THING_CACHE])
    override fun findByThingId(id: ThingId): Optional<Thing> = neo4jRepository.findById(id).map(Neo4jThing::toThing)

    override fun findAll(pageable: Pageable): Page<Thing> = neo4jRepository.findAll(pageable).map(Neo4jThing::toThing)

    override fun existsAll(ids: Set<ThingId>): Boolean = neo4jRepository.existsAll(ids)
}
