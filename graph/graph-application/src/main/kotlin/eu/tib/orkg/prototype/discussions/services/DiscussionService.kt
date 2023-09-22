package eu.tib.orkg.prototype.discussions.services

import eu.tib.orkg.prototype.auth.api.FindUserUseCases
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.discussions.api.CreateDiscussionCommentUseCase
import eu.tib.orkg.prototype.discussions.api.DiscussionUseCases
import eu.tib.orkg.prototype.discussions.application.InvalidContent
import eu.tib.orkg.prototype.discussions.application.TopicNotFound
import eu.tib.orkg.prototype.discussions.application.Unauthorized
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.discussions.spi.DiscussionCommentRepository
import eu.tib.orkg.prototype.statements.application.UserNotFound
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import java.util.regex.Pattern
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val orkgUrlPattern = Regex("""https?://(?:(?:www|incubating|sandbox)\.)?orkg\.org.*""")
private val urlPattern = Pattern.compile("""https?://.*|www\..+""")

@Service
class DiscussionService(
    private val repository: DiscussionCommentRepository,
    private val thingRepository: ThingRepository,
    private val userService: FindUserUseCases,
    private val clock: Clock = Clock.systemDefaultZone(),
) : DiscussionUseCases {
    override fun create(command: CreateDiscussionCommentUseCase.CreateCommand): DiscussionCommentId {
        thingRepository.findByThingId(command.topic)
            .filter { it !is Literal }
            .orElseThrow { TopicNotFound(command.topic) }
        if (!command.message.isValid())
            throw InvalidContent()
        val uuid = repository.nextIdentity()
        val comment =
            DiscussionComment(uuid, command.topic, command.message, command.createdBy, OffsetDateTime.now(clock))
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
        val user = userService.findById(contributorId.value)
            .orElseThrow { UserNotFound(contributorId.value) }
        repository.findById(id).ifPresent { comment ->
            if (!comment.isOwnedBy(ContributorId(user.id)) && !user.isAdmin) {
                throw Unauthorized()
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
