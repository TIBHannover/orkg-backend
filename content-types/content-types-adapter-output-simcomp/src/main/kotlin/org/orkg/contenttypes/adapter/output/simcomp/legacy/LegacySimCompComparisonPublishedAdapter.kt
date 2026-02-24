package org.orkg.contenttypes.adapter.output.simcomp.legacy

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.legacy.LegacyPublishedComparison
import org.orkg.contenttypes.output.legacy.LegacyComparisonPublishedRepository
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
class LegacySimCompComparisonPublishedAdapter(
    private val objectMapper: ObjectMapper,
    private val repository: SimCompThingRepository,
) : LegacyComparisonPublishedRepository {
    override fun findById(id: ThingId): Optional<LegacyPublishedComparison> =
        repository.findById(id, ThingType.COMPARISON).map { it.toLegacyPublishedComparison(objectMapper) }

    override fun save(comparison: LegacyPublishedComparison) {
        repository.save(comparison.id, ThingType.COMPARISON, comparison.data, comparison.config)
    }
}
