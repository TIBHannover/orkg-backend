package org.orkg.contenttypes.domain.legacy

import org.orkg.common.ThingId

data class LegacyPublishedComparison(
    val id: ThingId,
    val config: LegacyComparisonConfig,
    val data: LegacyComparisonData,
)
