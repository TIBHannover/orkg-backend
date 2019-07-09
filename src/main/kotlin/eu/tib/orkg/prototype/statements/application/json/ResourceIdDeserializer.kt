package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.statements.domain.model.ResourceId

class ResourceIdDeserializer :
    JsonDeserializer<ResourceId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): ResourceId? =
        p?.valueAsString?.let {
            ResourceId(it)
        }
}
