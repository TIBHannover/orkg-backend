package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.contenttypes.adapter.input.rest.IdentifierMapDTO

class IdentifierMapDTOSerializer : JsonSerializer<IdentifierMapDTO>() {
    override fun serialize(
        value: IdentifierMapDTO?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writePOJO(value?.values)
    }
}
