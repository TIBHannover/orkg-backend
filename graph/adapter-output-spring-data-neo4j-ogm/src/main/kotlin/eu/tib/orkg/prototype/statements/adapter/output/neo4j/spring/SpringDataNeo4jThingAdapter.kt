package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThing
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.util.*
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
    override fun findByThingId(id: String): Optional<Thing> = neo4jRepository.findByThingId(id).map(Neo4jThing::toThing)

    override fun findAll(): Iterable<Thing> = neo4jRepository.findAll().map(Neo4jThing::toThing)

    override fun findAll(pageable: Pageable): Page<Thing> = neo4jRepository.findAll(pageable).map(Neo4jThing::toThing)
}
