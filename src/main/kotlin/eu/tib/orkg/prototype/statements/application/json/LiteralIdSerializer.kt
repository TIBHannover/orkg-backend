package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import eu.tib.orkg.prototype.statements.domain.model.*

class LiteralIdSerializer : JsonSerializer<LiteralId>() {

    override fun serialize(
        value: LiteralId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
