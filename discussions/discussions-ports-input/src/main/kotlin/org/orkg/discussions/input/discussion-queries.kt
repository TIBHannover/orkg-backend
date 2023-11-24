package org.orkg.discussions.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.discussions.domain.DiscussionComment
import org.orkg.discussions.domain.DiscussionCommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveDiscussionUseCase {
    fun findByTopicAndCommentId(topic: ThingId, id: DiscussionCommentId): Optional<DiscussionComment>
    fun findAllByTopic(topic: ThingId, pageable: Pageable): Page<DiscussionComment>
}
