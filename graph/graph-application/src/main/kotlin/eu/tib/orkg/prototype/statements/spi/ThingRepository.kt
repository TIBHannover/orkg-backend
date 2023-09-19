package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ThingRepository {
    // legacy methods:
    fun findByThingId(id: ThingId): Optional<Thing>
    fun findAll(): Iterable<Thing>
    fun findAll(pageable: Pageable): Page<Thing>
    fun existsAll(ids: Set<ThingId>): Boolean
}
