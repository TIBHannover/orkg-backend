package org.orkg.community.adapter.input.rest.json

import org.orkg.common.exceptions.InvalidUUID
import org.orkg.community.domain.ConferenceSeriesId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class ConferenceSeriesIdDeserializer : ValueDeserializer<ConferenceSeriesId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ConferenceSeriesId? =
        p?.valueAsString?.let {
            try {
                ConferenceSeriesId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
