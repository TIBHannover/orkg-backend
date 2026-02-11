package org.orkg.mediastorage.adapter.input.serialization.json

import org.orkg.mediastorage.domain.ImageId
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class ImageIdSerializer : ValueSerializer<ImageId>() {
    override fun serialize(
        value: ImageId?,
        gen: JsonGenerator?,
        serializers: SerializationContext?,
    ) {
        gen?.writeString(value.toString())
    }
}
