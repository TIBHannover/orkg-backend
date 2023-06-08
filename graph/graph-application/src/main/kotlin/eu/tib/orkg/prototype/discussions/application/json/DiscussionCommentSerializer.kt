package eu.tib.orkg.prototype.discussions.application.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId

class DiscussionCommentIdSerializer : JsonSerializer<DiscussionCommentId>() {
    override fun serialize(
        value: DiscussionCommentId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
