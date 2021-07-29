package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jThingService(
    private val neo4jThingService: Neo4jThingRepository
) : ThingService {

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
