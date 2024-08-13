package org.orkg.contenttypes.adapter.output.simcomp

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.contenttypes.adapter.output.simcomp.internal.BaseThing
import org.orkg.contenttypes.domain.PublishedContentType

fun BaseThing.toPublishedContentType(objectMapper: ObjectMapper): PublishedContentType =
    objectMapper.treeToValue(data, PublishedContentType::class.java)
