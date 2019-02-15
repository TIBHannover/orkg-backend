package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import eu.tib.orkg.prototype.statements.domain.model.*

class PredicateIdDeserializer :
    JsonDeserializer<PredicateId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): PredicateId? =
        p?.valueAsString?.let { PredicateId(it) }
}
