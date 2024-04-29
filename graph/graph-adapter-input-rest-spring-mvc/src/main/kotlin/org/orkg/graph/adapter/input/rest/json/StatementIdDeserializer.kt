package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.graph.domain.StatementId

class StatementIdDeserializer :
    JsonDeserializer<StatementId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): StatementId? =
        p?.valueAsString?.let { StatementId(it) }
}
