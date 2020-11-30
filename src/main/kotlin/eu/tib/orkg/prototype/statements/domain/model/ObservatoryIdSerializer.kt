package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class ObservatoryIdSerializer : JsonSerializer<ObservatoryId>() {
    override fun serialize(
        value: ObservatoryId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
