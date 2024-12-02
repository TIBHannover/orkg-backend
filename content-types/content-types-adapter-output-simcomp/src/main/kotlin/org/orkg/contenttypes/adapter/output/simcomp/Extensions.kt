package org.orkg.contenttypes.adapter.output.simcomp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.orkg.contenttypes.adapter.output.simcomp.internal.BaseThing
import org.orkg.contenttypes.domain.PublishedComparison
import org.orkg.contenttypes.domain.PublishedContentType

fun BaseThing.toPublishedContentType(objectMapper: ObjectMapper): PublishedContentType =
    objectMapper.treeToValue((data as ObjectNode).put("id", thingKey.value), PublishedContentType::class.java)

fun BaseThing.toPublishedComparison(objectMapper: ObjectMapper): PublishedComparison =
    objectMapper.treeToValue(data, PublishedComparison::class.java)
