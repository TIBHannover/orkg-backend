package org.orkg.discussions.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.discussions.domain.DiscussionCommentId

data class DiscussionCommentRepresentation(
    val id: DiscussionCommentId,
    val message: String,
    @JsonProperty("created_by")
    val createdBy: ContributorId,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime
)
