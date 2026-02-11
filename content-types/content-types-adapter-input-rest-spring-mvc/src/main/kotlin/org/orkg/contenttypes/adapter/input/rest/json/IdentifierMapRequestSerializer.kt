package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.contenttypes.adapter.input.rest.IdentifierMapRequest
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class IdentifierMapRequestSerializer : ValueSerializer<IdentifierMapRequest>() {
    override fun serialize(
        value: IdentifierMapRequest?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writePOJO(value?.values)
    }
}
