package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.statements.domain.model.StatementId

class StatementIdDeserializer :
    JsonDeserializer<StatementId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): StatementId? =
        p?.valueAsString?.let { StatementId(it) }
}
