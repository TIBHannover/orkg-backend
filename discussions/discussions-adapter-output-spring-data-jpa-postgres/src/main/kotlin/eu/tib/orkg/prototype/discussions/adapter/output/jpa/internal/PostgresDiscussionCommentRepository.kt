package eu.tib.orkg.prototype.discussions.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface PostgresDiscussionCommentRepository : JpaRepository<DiscussionCommentEntity, UUID> {
    fun findAllByTopicOrderByCreatedAtDesc(@Param("topic") topic: String, pageable: Pageable): Page<DiscussionCommentEntity>
}
