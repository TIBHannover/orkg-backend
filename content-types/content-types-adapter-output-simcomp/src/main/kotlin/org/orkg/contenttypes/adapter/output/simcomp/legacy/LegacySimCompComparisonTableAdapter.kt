package org.orkg.contenttypes.adapter.output.simcomp.legacy

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.legacy.LegacyComparisonTable
import org.orkg.contenttypes.output.legacy.LegacyComparisonTableRepository
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
class LegacySimCompComparisonTableAdapter(
    private val objectMapper: ObjectMapper,
    private val repository: SimCompThingRepository,
) : LegacyComparisonTableRepository {
    override fun findById(id: ThingId): Optional<LegacyComparisonTable> =
        repository.findById(id, ThingType.DRAFT_COMPARISON).map { it.toLegacyComparisonTable(objectMapper) }

    override fun save(comparison: LegacyComparisonTable) {
        repository.save(comparison.id, ThingType.DRAFT_COMPARISON, comparison.data, comparison.config)
    }

    override fun update(comparison: LegacyComparisonTable) {
        repository.update(comparison.id, ThingType.DRAFT_COMPARISON, comparison.data, comparison.config)
    }
}
