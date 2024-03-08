package org.orkg.discussions.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*
import org.orkg.common.exceptions.InvalidUUID
import org.orkg.discussions.domain.DiscussionCommentId

class DiscussionCommentIdDeserializer : JsonDeserializer<DiscussionCommentId>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): DiscussionCommentId? =
        p?.valueAsString?.let {
            try {
                DiscussionCommentId(UUID.fromString(it))
            } catch (exception: IllegalArgumentException) {
                throw InvalidUUID(it, exception)
            }
        }
}
