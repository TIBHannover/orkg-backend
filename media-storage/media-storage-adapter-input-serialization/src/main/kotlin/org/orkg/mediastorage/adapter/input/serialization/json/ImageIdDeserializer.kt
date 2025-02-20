package org.orkg.mediastorage.adapter.input.serialization.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.orkg.common.exceptions.InvalidUUID
import org.orkg.mediastorage.domain.ImageId
import java.util.UUID

class ImageIdDeserializer : JsonDeserializer<ImageId>() {
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
