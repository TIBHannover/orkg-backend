package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.community.domain.ObservatoryFilterId
import java.util.*
import org.orkg.common.exceptions.InvalidUUID

class ObservatoryFilterIdDeserializer :
    JsonDeserializer<ObservatoryFilterId>() {

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): ObservatoryFilterId? =
        p?.valueAsString?.let {
            try {
                ObservatoryFilterId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
