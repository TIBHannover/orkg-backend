package org.orkg.graph.adapter.input.rest.json

import org.orkg.graph.domain.StatementId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class StatementIdSerializer : ValueSerializer<StatementId>() {
    override fun serialize(
        value: StatementId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
