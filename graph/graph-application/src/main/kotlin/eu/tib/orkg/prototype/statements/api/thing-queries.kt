package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*

interface RetrieveThingUseCase {
    fun exists(id: ThingId): Boolean
    fun findByThingId(id: ThingId): Optional<Thing>
}
