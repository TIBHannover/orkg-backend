package org.orkg.contenttypes.adapter.output.simcomp.legacy

import org.orkg.contenttypes.adapter.output.simcomp.internal.BaseThing
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.contenttypes.domain.legacy.LegacyComparisonData
import org.orkg.contenttypes.domain.legacy.LegacyComparisonTable
import org.orkg.contenttypes.domain.legacy.LegacyPublishedComparison
import tools.jackson.databind.ObjectMapper

fun BaseThing.toLegacyPublishedComparison(objectMapper: ObjectMapper): LegacyPublishedComparison =
    LegacyPublishedComparison(
        id = thingKey,
        config = objectMapper.treeToValue(config, LegacyComparisonConfig::class.java),
        data = objectMapper.treeToValue(data, LegacyComparisonData::class.java)
    )

fun BaseThing.toLegacyComparisonTable(objectMapper: ObjectMapper): LegacyComparisonTable =
    LegacyComparisonTable(
        id = thingKey,
        config = objectMapper.treeToValue(config, LegacyComparisonConfig::class.java),
        data = objectMapper.treeToValue(data, LegacyComparisonData::class.java)
    )
