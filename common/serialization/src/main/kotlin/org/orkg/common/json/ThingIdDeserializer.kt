package org.orkg.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.common.ThingId

class ThingIdDeserializer : JsonDeserializer<ThingId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): ThingId? =
        p?.valueAsString?.let {
            ThingId(it)
        }
}
