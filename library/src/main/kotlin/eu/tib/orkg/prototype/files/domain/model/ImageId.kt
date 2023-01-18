package eu.tib.orkg.prototype.files.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.files.application.json.ImageIdDeserializer
import eu.tib.orkg.prototype.files.application.json.ImageIdSerializer
import java.util.UUID

@JsonDeserialize(using = ImageIdDeserializer::class)
@JsonSerialize(using = ImageIdSerializer::class)
data class ImageId(val value: UUID) {
    override fun toString() = value.toString()
}
