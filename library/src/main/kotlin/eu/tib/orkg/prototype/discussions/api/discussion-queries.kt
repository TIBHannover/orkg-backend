package eu.tib.orkg.prototype.discussions.api

import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveDiscussionUseCase {
    fun findByTopicAndCommentId(topic: ThingId, id: DiscussionCommentId): Optional<DiscussionCommentRepresentation>
    fun findAllByTopic(topic: ThingId, pageable: Pageable): Page<DiscussionCommentRepresentation>
}
