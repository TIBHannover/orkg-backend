package org.orkg.community.adapter.input.rest.json

import org.orkg.community.domain.ConferenceSeriesId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ConferenceSeriesIdSerializer : ValueSerializer<ConferenceSeriesId>() {
    override fun serialize(
        value: ConferenceSeriesId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
