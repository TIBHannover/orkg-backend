package org.orkg.mediastorage.adapter.input.serialization.json

import org.orkg.common.exceptions.InvalidUUID
import org.orkg.mediastorage.domain.ImageId
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class ImageIdDeserializer : ValueDeserializer<ImageId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ImageId? =
        p?.valueAsString?.let {
            try {
                ImageId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
