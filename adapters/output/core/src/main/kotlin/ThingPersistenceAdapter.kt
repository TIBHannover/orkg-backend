package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.ports.ThingRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ThingPersistenceAdapter(
    //The dependency injector's name should be changed - Not sure
    private val neo4jThingService: Neo4jThingRepository
): ThingRepository {

    override fun findAll(): Iterable<Thing> =
        neo4jThingService.findAll().map { it.toThing() }

    override fun findAll(pageable: Pageable): Iterable<Thing> =
        neo4jThingService.findAll(pageable)
            .map { it.toThing() }
            .content

    override fun findById(id: String?): Optional<Thing> =
        neo4jThingService.findByThingId(id)
            .map { it.toThing() }
}
