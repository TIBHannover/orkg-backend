package eu.tib.orkg.prototype.community.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.statements.application.InvalidUUID
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId
import java.util.UUID

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
