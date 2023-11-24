package org.orkg.discussions.adapter.output.jpa

import java.util.*
import org.orkg.common.ThingId
import org.orkg.discussions.adapter.output.jpa.internal.DiscussionCommentEntity
import org.orkg.discussions.adapter.output.jpa.internal.PostgresDiscussionCommentRepository
import org.orkg.discussions.domain.DiscussionComment
import org.orkg.discussions.domain.DiscussionCommentId
import org.orkg.discussions.output.DiscussionCommentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringJpaPostgresDiscussionCommentAdapter(
    private val repository: PostgresDiscussionCommentRepository
) : DiscussionCommentRepository {
    override fun save(comment: DiscussionComment) {
        repository.save(comment.toDiscussionCommentEntity())
    }

    override fun findById(id: DiscussionCommentId): Optional<DiscussionComment> =
        repository.findById(id.value).map(DiscussionCommentEntity::toDiscussionComment)

    override fun findAllByTopic(topic: ThingId, pageable: Pageable): Page<DiscussionComment> =
        repository.findAllByTopicOrderByCreatedAtDesc(topic.value, pageable)
            .map(DiscussionCommentEntity::toDiscussionComment)

    override fun nextIdentity(): DiscussionCommentId {
        var uuid: UUID?
        do {
            uuid = UUID.randomUUID()
        } while (repository.existsById(uuid!!))
        return DiscussionCommentId(uuid)
    }

    override fun deleteById(id: DiscussionCommentId) = repository.deleteById(id.value)

    override fun deleteAll() = repository.deleteAll()

    private fun DiscussionComment.toDiscussionCommentEntity(): DiscussionCommentEntity =
        repository.findById(id.value).orElse(DiscussionCommentEntity()).apply {
            id = this@toDiscussionCommentEntity.id.value
            topic = this@toDiscussionCommentEntity.topic.value
            message = this@toDiscussionCommentEntity.message
            createdBy = this@toDiscussionCommentEntity.createdBy.value
            createdAt = this@toDiscussionCommentEntity.createdAt
        }
}
