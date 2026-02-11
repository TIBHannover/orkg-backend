package org.orkg.common.json

import org.orkg.common.ObservatoryId
import org.orkg.common.exceptions.InvalidUUID
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class ObservatoryIdDeserializer : ValueDeserializer<ObservatoryId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ObservatoryId? =
        p?.valueAsString?.let {
            try {
                ObservatoryId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
