package eu.tib.orkg.prototype.statements.domain.model

import org.springframework.data.domain.Pageable
import java.util.Optional

interface ThingService {

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
