package org.orkg.graph.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import java.util.Optional

interface ThingUseCases : RetrieveThingUseCase

interface RetrieveThingUseCase {
    fun existsById(id: ThingId): Boolean

    fun findById(id: ThingId): Optional<Thing>
}
