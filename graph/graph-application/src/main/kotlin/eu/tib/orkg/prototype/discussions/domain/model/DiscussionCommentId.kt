package eu.tib.orkg.prototype.discussions.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.discussions.application.json.DiscussionCommentIdDeserializer
import eu.tib.orkg.prototype.discussions.application.json.DiscussionCommentIdSerializer
import java.util.*

@JsonDeserialize(using = DiscussionCommentIdDeserializer::class)
@JsonSerialize(using = DiscussionCommentIdSerializer::class)
data class DiscussionCommentId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    override fun toString() = value.toString()
}
