package eu.tib.orkg.prototype.discussions.adapter.output.jpa.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "discussion_comments")
class DiscussionCommentEntity() {
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
