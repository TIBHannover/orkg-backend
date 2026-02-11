package org.orkg.common.json

import org.orkg.common.ObservatoryId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ObservatoryIdSerializer : ValueSerializer<ObservatoryId>() {
    override fun serialize(
        value: ObservatoryId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
