package org.orkg.mediastorage.adapter.input.serialization.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.mediastorage.domain.ImageId

class ImageIdSerializer : JsonSerializer<ImageId>() {
    override fun serialize(
        value: ImageId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        gen?.writeString(value.toString())
    }
}
