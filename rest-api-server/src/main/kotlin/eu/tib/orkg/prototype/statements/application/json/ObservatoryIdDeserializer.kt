package eu.tib.orkg.prototype.statements.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.statements.application.InvalidUUID
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import java.util.UUID

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
