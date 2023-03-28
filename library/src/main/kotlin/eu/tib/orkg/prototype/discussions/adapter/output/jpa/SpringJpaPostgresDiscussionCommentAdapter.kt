package eu.tib.orkg.prototype.discussions.adapter.output.jpa

import eu.tib.orkg.prototype.discussions.adapter.output.jpa.internal.DiscussionCommentEntity
import eu.tib.orkg.prototype.discussions.adapter.output.jpa.internal.PostgresDiscussionCommentRepository
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.discussions.spi.DiscussionCommentRepository
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringJpaPostgresDiscussionCommentAdapter(
    @Qualifier("postgresDiscussionCommentRepository")
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
