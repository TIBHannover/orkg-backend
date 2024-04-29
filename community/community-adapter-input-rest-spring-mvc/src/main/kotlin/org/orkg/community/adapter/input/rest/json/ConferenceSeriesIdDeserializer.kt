package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*
import org.orkg.common.exceptions.InvalidUUID
import org.orkg.community.domain.ConferenceSeriesId

class ConferenceSeriesIdDeserializer :
    JsonDeserializer<ConferenceSeriesId>() {

        override fun deserialize(
            p: JsonParser?,
            ctxt: DeserializationContext?
        ): ConferenceSeriesId? =
            p?.valueAsString?.let {
                try {
                    ConferenceSeriesId(UUID.fromString(it))
                } catch (exception: IllegalArgumentException) {
                    throw InvalidUUID(it, exception)
                }
            }
}
