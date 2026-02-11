package org.orkg.common.json

import org.orkg.common.ThingId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ThingIdSerializer : ValueSerializer<ThingId>() {
    override fun serialize(
        value: ThingId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
