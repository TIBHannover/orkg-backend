package org.orkg.discussions.domain

import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import java.util.regex.Pattern
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.community.output.ContributorRepository
import org.orkg.discussions.input.CreateDiscussionCommentUseCase
import org.orkg.discussions.input.DiscussionUseCases
import org.orkg.discussions.output.DiscussionCommentRepository
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.UserNotFound
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val orkgUrlPattern = Regex("""https?://(?:(?:www|incubating|sandbox)\.)?orkg\.org.*""")
private val urlPattern = Pattern.compile("""https?://.*|www\..+""")

@Service
class DiscussionService(
    private val repository: DiscussionCommentRepository,
    private val thingRepository: ThingRepository,
    private val contributorRepository: ContributorRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : DiscussionUseCases {
    override fun create(command: CreateDiscussionCommentUseCase.CreateCommand): DiscussionCommentId {
        thingRepository.findByThingId(command.topic)
            .filter { it !is Literal }
            .orElseThrow { TopicNotFound(command.topic) }
        if (!command.message.isValid())
            throw InvalidContent()
        val uuid = repository.nextIdentity()
        val comment = DiscussionComment(uuid, command.topic, command.message, command.createdBy, OffsetDateTime.now(clock))
        repository.save(comment)
        return uuid
    }

    override fun findByTopicAndCommentId(topic: ThingId, id: DiscussionCommentId): Optional<DiscussionComment> =
        thingRepository.findByThingId(topic)
            .filter { it !is Literal }
            .map { repository.findById(id) }
            .orElseThrow { TopicNotFound(topic) }

    override fun findAllByTopic(topic: ThingId, pageable: Pageable): Page<DiscussionComment> =
        thingRepository.findByThingId(topic)
            .filter { it !is Literal }
            .map { repository.findAllByTopic(topic, pageable) }
            .orElseThrow { TopicNotFound(topic) }

    override fun delete(contributorId: ContributorId, topic: ThingId, id: DiscussionCommentId) {
        val contributor = contributorRepository.findById(contributorId)
            .orElseThrow { UserNotFound(contributorId.value) }
        repository.findById(id).ifPresent { comment ->
            if (!comment.isOwnedBy(contributor.id) && !contributor.isCurator) {
                throw NeitherOwnerNorCurator(contributorId)
            }
            repository.deleteById(id)
        }
    }

    private fun String?.isValid(): Boolean {
        if (this.isNullOrBlank()) return false
        val stripped = replace(orkgUrlPattern, "")
        val matcher = urlPattern.matcher(stripped)
        return !matcher.find()
    }
}
