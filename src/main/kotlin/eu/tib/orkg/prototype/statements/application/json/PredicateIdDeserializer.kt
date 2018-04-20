package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.statements.domain.model.PredicateId

class PredicateIdDeserializer :
    JsonDeserializer<PredicateId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): PredicateId? {
        return p?.valueAsString?.let {
            PredicateId(it)
        }
    }
}
