package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.graph.domain.StatementId

class StatementIdSerializer : JsonSerializer<StatementId>() {
    override fun serialize(
        value: StatementId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writeString(value.toString())
    }
}
