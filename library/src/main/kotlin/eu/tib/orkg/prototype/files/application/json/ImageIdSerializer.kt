package eu.tib.orkg.prototype.files.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.files.domain.model.ImageId

class ImageIdSerializer : JsonSerializer<ImageId>() {
    override fun serialize(
        value: ImageId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
