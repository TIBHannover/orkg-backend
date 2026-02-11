package org.orkg.common.json

import org.orkg.common.ThingId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class ThingIdDeserializer : ValueDeserializer<ThingId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ThingId? =
        p?.valueAsString?.let {
            ThingId(it)
        }
}
