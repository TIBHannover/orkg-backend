package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.graph.domain.DynamicLabel
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class DynamicLabelSerializer : ValueSerializer<DynamicLabel>() {
    override fun serialize(value: DynamicLabel?, gen: JsonGenerator?, serializers: SerializationContext?) {
        gen?.writeString(value?.template)
    }
}
