package org.orkg.graph.adapter.input.rest.json

import org.orkg.graph.domain.StatementId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class StatementIdDeserializer : ValueDeserializer<StatementId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): StatementId? =
        p?.valueAsString?.let { StatementId(it) }
}
