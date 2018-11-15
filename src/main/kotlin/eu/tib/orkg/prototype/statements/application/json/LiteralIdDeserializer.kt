package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import eu.tib.orkg.prototype.statements.domain.model.*

class LiteralIdDeserializer :
    JsonDeserializer<LiteralId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): LiteralId? =
        p?.valueAsLong?.let {
            LiteralId(it)
        }
}
