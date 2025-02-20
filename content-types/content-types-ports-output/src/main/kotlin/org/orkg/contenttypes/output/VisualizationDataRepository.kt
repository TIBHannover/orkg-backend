package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationData
import java.util.Optional

interface VisualizationDataRepository {
    fun findById(id: ThingId): Optional<VisualizationData>

    fun save(data: VisualizationData)
}
