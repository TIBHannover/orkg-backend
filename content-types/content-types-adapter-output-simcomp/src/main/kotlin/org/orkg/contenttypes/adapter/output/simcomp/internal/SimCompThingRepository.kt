package org.orkg.contenttypes.adapter.output.simcomp.internal

import org.orkg.common.ThingId
import java.util.Optional

interface SimCompThingRepository {
    fun findById(id: ThingId, type: ThingType): Optional<BaseThing>

    fun save(id: ThingId, type: ThingType, data: Any, config: Any = emptyMap<String, Any>())

    fun update(id: ThingId, type: ThingType, data: Any, config: Any = emptyMap<String, Any>())
}
