package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.contenttypes.adapter.output.simcomp.internal.BaseThing
import org.orkg.contenttypes.domain.ComparisonConfig
import org.orkg.contenttypes.domain.ComparisonData
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.PublishedComparison
import org.orkg.contenttypes.domain.PublishedContentType
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode

fun BaseThing.toPublishedContentType(objectMapper: ObjectMapper): PublishedContentType =
    objectMapper.treeToValue((data as ObjectNode).put("id", thingKey.value), PublishedContentType::class.java)

fun BaseThing.toPublishedComparison(objectMapper: ObjectMapper): PublishedComparison =
    PublishedComparison(
        id = thingKey,
        config = objectMapper.treeToValue(config, ComparisonConfig::class.java),
        data = objectMapper.treeToValue(data, ComparisonData::class.java)
    )

fun BaseThing.toComparisonTable(objectMapper: ObjectMapper): ComparisonTable =
    ComparisonTable(
        id = thingKey,
        config = objectMapper.treeToValue(config, ComparisonConfig::class.java),
        data = objectMapper.treeToValue(data, ComparisonData::class.java)
    )
