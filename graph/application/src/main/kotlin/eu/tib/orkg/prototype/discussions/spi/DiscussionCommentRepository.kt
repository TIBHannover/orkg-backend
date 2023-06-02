package eu.tib.orkg.prototype.discussions.spi

import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
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
