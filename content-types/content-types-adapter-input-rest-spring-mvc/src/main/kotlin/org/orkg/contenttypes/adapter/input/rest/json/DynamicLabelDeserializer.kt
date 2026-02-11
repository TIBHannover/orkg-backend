package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.graph.domain.DynamicLabel
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class DynamicLabelDeserializer : ValueDeserializer<DynamicLabel>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): DynamicLabel? =
        p?.valueAsString?.let(::DynamicLabel)
}
