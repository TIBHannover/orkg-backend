package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class PublishedComparison(
    val id: ThingId,
    val config: ComparisonConfig,
    val data: ComparisonData,
)
