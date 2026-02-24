package org.orkg.contenttypes.adapter.output.simcomp

import org.orkg.contenttypes.adapter.output.simcomp.internal.BaseThing
import org.orkg.contenttypes.domain.PublishedContentType
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode

fun BaseThing.toPublishedContentType(objectMapper: ObjectMapper): PublishedContentType =
    objectMapper.treeToValue((data as ObjectNode).put("id", thingKey.value), PublishedContentType::class.java)
