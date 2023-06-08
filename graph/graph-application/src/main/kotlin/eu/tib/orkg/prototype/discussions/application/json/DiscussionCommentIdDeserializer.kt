package eu.tib.orkg.prototype.discussions.application.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.application.InvalidUUID
import java.util.*

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
