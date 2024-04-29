package org.orkg.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.exceptions.InvalidUUID

class ObservatoryIdDeserializer :
    JsonDeserializer<ObservatoryId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): ObservatoryId? =
        p?.valueAsString?.let {
            try {
                ObservatoryId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
