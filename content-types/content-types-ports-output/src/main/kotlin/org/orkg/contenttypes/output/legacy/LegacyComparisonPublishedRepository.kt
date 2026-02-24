package org.orkg.contenttypes.output.legacy

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.legacy.LegacyPublishedComparison
import java.util.Optional

interface LegacyComparisonPublishedRepository {
    fun findById(id: ThingId): Optional<LegacyPublishedComparison>

    fun save(comparison: LegacyPublishedComparison)
}
