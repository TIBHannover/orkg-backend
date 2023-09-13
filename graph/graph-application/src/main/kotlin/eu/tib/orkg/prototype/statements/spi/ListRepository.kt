package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ListRepository {
    fun save(list: List, contributorId: ContributorId)
    fun findById(id: ThingId): Optional<List>
    fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing>
    fun nextIdentity(): ThingId
    fun exists(id: ThingId): Boolean
    fun delete(id: ThingId)
}
