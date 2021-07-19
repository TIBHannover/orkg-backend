package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ThingRepository {

    /**
     * Find all things.
     */
    fun findAll(): Iterable<Thing>

    /**
     * Find all things (Paginated).
     */
    fun findAll(pageable: Pageable): Iterable<Thing>

    /**
     * Find a thing by its ID.
     */
    fun findById(id: String?): Optional<Thing>
}
