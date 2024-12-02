package org.orkg.discussions.adapter.output.jpa.internal

import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.discussions.domain.DiscussionComment
import org.orkg.discussions.domain.DiscussionCommentId

@Entity
@Table(name = "discussion_comments")
class DiscussionCommentEntity {
    @Id
    var id: UUID? = null

    @NotBlank
    var topic: String? = null

    @NotBlank
    var message: String? = null

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    fun toDiscussionComment() =
        DiscussionComment(
            id = DiscussionCommentId(id!!),
            topic = ThingId(topic!!),
            message = message!!,
            createdBy = ContributorId(createdBy!!),
            createdAt = createdAt!!
        )
}
