package org.orkg.discussions.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.discussions.domain.DiscussionComment
import org.orkg.discussions.domain.DiscussionCommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface DiscussionCommentRepository {
    fun save(comment: DiscussionComment)
    fun findById(id: DiscussionCommentId): Optional<DiscussionComment>
    fun findAllByTopic(topic: ThingId, pageable: Pageable): Page<DiscussionComment>
    fun nextIdentity(): DiscussionCommentId
    fun deleteById(id: DiscussionCommentId)
    fun deleteAll()
}
