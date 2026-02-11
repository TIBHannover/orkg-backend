package org.orkg.community.adapter.input.rest.json

import org.orkg.community.domain.ObservatoryFilterId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ObservatoryFilterIdSerializer : ValueSerializer<ObservatoryFilterId>() {
    override fun serialize(
        value: ObservatoryFilterId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
