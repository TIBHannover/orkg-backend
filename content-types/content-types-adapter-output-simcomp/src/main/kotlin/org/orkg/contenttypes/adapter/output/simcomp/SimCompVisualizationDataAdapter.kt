package org.orkg.contenttypes.adapter.output.simcomp

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.VisualizationData
import org.orkg.contenttypes.output.VisualizationDataRepository
import org.springframework.stereotype.Component

@Component
class SimCompVisualizationDataAdapter(
    private val repository: SimCompThingRepository
) : VisualizationDataRepository {
    override fun findById(id: ThingId): Optional<VisualizationData> =
        repository.findById(id, ThingType.VISUALIZATION).map { VisualizationData.from(it.data) }

    override fun save(data: VisualizationData) {
        repository.save(data.id, ThingType.VISUALIZATION, data.data)
    }
}
