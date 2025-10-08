package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.graph.domain.DynamicLabel

class DynamicLabelDeserializer : JsonDeserializer<DynamicLabel>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): DynamicLabel? =
        p?.valueAsString?.let(::DynamicLabel)
}
