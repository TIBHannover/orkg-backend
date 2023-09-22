package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveListUseCase {
    fun findById(id: ThingId): Optional<List>
    fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing>
    fun exists(id: ThingId): Boolean
}
