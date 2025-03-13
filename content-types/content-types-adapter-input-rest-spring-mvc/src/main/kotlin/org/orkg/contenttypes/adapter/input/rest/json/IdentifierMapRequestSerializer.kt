package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.contenttypes.adapter.input.rest.IdentifierMapRequest

class IdentifierMapRequestSerializer : JsonSerializer<IdentifierMapRequest>() {
    override fun serialize(
        value: IdentifierMapRequest?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writePOJO(value?.values)
    }
}
