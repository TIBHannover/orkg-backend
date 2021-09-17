package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.ports.ThingRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ThingPersistenceAdapter(
    private val neo4jThingRepository: Neo4jThingRepository
): ThingRepository {

    override fun findAll(): Iterable<Thing> = neo4jThingRepository.findAll().map(Neo4jThing::toThing)

    override fun findAll(pageable: Pageable): Iterable<Thing> =
        neo4jThingRepository.findAll(pageable).map(Neo4jThing::toThing)

    override fun findById(id: String?): Optional<Thing> =
        neo4jThingRepository.findByThingId(id).map(Neo4jThing::toThing)
}
