package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingService
import eu.tib.orkg.prototype.statements.ports.ThingRepository
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jThingService(
    private val thingRepository: ThingRepository
) : ThingService {

    override fun findAll(): Iterable<Thing> = thingRepository.findAll()

    override fun findAll(pageable: Pageable): Iterable<Thing> = thingRepository.findAll(pageable)

    override fun findById(id: String?): Optional<Thing> = thingRepository.findById(id)
}
