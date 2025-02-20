package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.community.domain.ConferenceSeriesId

class ConferenceSeriesIdSerializer : JsonSerializer<ConferenceSeriesId>() {
    override fun serialize(
        value: ConferenceSeriesId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writeString(value.toString())
    }
}
