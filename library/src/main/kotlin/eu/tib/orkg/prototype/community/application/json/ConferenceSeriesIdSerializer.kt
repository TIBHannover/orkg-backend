package eu.tib.orkg.prototype.community.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId

class ConferenceSeriesIdSerializer : JsonSerializer<ConferenceSeriesId>() {
    override fun serialize(
        value: ConferenceSeriesId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
