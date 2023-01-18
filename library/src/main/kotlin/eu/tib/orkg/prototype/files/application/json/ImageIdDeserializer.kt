package eu.tib.orkg.prototype.files.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.application.InvalidUUID
import java.util.*

class ImageIdDeserializer : JsonDeserializer<ImageId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): ImageId? =
        p?.valueAsString?.let {
            try {
                ImageId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
