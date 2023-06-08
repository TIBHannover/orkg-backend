package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.statements.domain.model.StatementId

class StatementIdSerializer : JsonSerializer<StatementId>() {

    override fun serialize(
        value: StatementId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
