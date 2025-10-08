package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.graph.domain.DynamicLabel

class DynamicLabelSerializer : JsonSerializer<DynamicLabel>() {
    override fun serialize(value: DynamicLabel?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.template)
    }
}
