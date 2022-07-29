package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryThingRepository : ThingRepository {
    override fun findByThingId(id: String?): Optional<Thing> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Thing> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Thing> {
        TODO("Not yet implemented")
    }
}
