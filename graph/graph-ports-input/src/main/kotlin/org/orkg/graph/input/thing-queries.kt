package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

interface RetrieveThingUseCase {
    fun existsById(id: ThingId): Boolean
    fun findById(id: ThingId): Optional<Thing>
}
