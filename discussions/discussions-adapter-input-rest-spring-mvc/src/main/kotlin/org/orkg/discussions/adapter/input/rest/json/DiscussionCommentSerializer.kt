package org.orkg.discussions.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.orkg.discussions.domain.DiscussionCommentId

class DiscussionCommentIdSerializer : JsonSerializer<DiscussionCommentId>() {
    override fun serialize(
        value: DiscussionCommentId?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value.toString())
    }
}
