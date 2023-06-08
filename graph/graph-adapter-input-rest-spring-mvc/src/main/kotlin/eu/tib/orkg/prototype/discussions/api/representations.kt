package eu.tib.orkg.prototype.discussions.api

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import java.time.OffsetDateTime

interface DiscussionCommentRepresentation {
    val id: DiscussionCommentId
    val message: String
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
}
