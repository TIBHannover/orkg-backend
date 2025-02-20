package org.orkg.contenttypes.adapter.output.simcomp

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.simcomp.internal.SimCompThingRepository
import org.orkg.contenttypes.adapter.output.simcomp.internal.ThingType
import org.orkg.contenttypes.domain.PublishedComparison
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class SimCompComparisonPublishedAdapter(
    private val objectMapper: ObjectMapper,
    private val repository: SimCompThingRepository,
) : ComparisonPublishedRepository {
    override fun findById(id: ThingId): Optional<PublishedComparison> =
        repository.findById(id, ThingType.COMPARISON).map { it.toPublishedComparison(objectMapper) }

    override fun save(comparison: PublishedComparison) {
        repository.save(comparison.id, ThingType.COMPARISON, comparison.data, comparison.config)
    }
}
