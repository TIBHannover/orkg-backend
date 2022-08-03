package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId

class ObservatoryIdSerializer : JsonSerializer<ObservatoryId>() {
    override fun serialize(
        value: ObservatoryId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
