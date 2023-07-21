package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.statements.domain.model.ThingId

class ThingIdSerializer : JsonSerializer<ThingId>() {

    override fun serialize(
        value: ThingId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
