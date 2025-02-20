package org.orkg.common.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.common.ThingId

class ThingIdSerializer : JsonSerializer<ThingId>() {
    override fun serialize(
        value: ThingId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writeString(value.toString())
    }
}
