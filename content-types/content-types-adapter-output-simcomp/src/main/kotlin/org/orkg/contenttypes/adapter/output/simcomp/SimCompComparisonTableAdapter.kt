package org.orkg.contenttypes.adapter.output.simcomp

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class SimCompComparisonTableAdapter(
    private val objectMapper: ObjectMapper,
    private val repository: SimCompThingRepository,
) : ComparisonTableRepository {
    override fun findById(id: ThingId): Optional<ComparisonTable> =
        repository.findById(id, ThingType.DRAFT_COMPARISON).map { it.toComparisonTable(objectMapper) }

    override fun save(comparison: ComparisonTable) {
        repository.save(comparison.id, ThingType.DRAFT_COMPARISON, comparison.data, comparison.config)
    }

    override fun update(comparison: ComparisonTable) {
        repository.update(comparison.id, ThingType.DRAFT_COMPARISON, comparison.data, comparison.config)
    }
}
