package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationData

interface VisualizationDataRepository {
    fun findById(id: ThingId): Optional<VisualizationData>
    fun save(data: VisualizationData)
}
