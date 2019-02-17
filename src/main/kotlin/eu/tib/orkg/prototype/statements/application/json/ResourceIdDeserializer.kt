package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import eu.tib.orkg.prototype.statements.domain.model.*

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
