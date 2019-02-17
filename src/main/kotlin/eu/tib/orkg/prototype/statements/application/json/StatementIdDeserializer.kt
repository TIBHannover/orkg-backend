package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import eu.tib.orkg.prototype.statements.domain.model.*

class StatementIdDeserializer :
    JsonDeserializer<StatementId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): StatementId? =
        p?.valueAsString?.let { StatementId(it) }
}
