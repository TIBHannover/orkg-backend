package org.orkg.community.adapter.input.rest.json

import org.orkg.common.exceptions.InvalidUUID
import org.orkg.community.domain.ObservatoryFilterId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class ObservatoryFilterIdDeserializer : ValueDeserializer<ObservatoryFilterId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ObservatoryFilterId? =
        p?.valueAsString?.let {
            try {
                ObservatoryFilterId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
