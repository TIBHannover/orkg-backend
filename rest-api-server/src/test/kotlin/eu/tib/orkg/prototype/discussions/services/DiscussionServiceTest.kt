package eu.tib.orkg.prototype.discussions.services

import eu.tib.orkg.prototype.auth.persistence.RoleEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.discussions.api.CreateDiscussionCommentUseCase
import eu.tib.orkg.prototype.discussions.application.InvalidContent
import eu.tib.orkg.prototype.discussions.application.TopicNotFound
import eu.tib.orkg.prototype.discussions.application.Unauthorized
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.discussions.spi.DiscussionCommentRepository
import eu.tib.orkg.prototype.statements.application.UserNotFound
import eu.tib.orkg.prototype.statements.domain.model.Clock
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

class DiscussionServiceTest {
    private val repository: DiscussionCommentRepository = mockk()
    private val thingRepository: ThingRepository = mockk()
    private val userService: UserService = mockk()
    private val staticClock: Clock = object : Clock {
        override fun now(): OffsetDateTime = OffsetDateTime.of(2022, 11, 14, 14, 9, 23, 12345, ZoneOffset.ofHours(1))
    }
    private val service = DiscussionService(repository, thingRepository, userService, staticClock)

    @Test
    fun `given a discussion comment is created, then it gets an id and is saved to the repository`() {
        val id = DiscussionCommentId(UUID.randomUUID())
        val topic = ThingId("C123")
        val comment = "Some comment"
        val contributor = ContributorId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } returns Optional.of(createClass().copy(id = topic))
        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } returns Unit

        service.create(CreateDiscussionCommentUseCase.CreateCommand(topic, comment, contributor))

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                DiscussionComment(
                    id = id,
                    topic = topic,
                    message = comment,
                    createdBy = contributor,
                    createdAt = staticClock.now()
                )
            )
        }
    }

    @Test
    fun `given a discussion comment is created, when the topic is invalid, then an exception is thrown`() {
        val topic = ThingId("C123")
        val comment = "Some comment"
        val contributor = ContributorId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } throws TopicNotFound(topic)

        shouldThrow<TopicNotFound> {
            service.create(CreateDiscussionCommentUseCase.CreateCommand(topic, comment, contributor))
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any()) }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "  ",
            "http://example.org",
            "https://example.org",
            "http://www.example.org",
            "https://www.example.org",
            "http://example.org/",
            "https://example.org/",
            "http://example.org/",
            "https://example.org/",
            "http://example.org/path/to/resource",
            "https://example.org/path/to/resource",
            "http://example.org/path/to/resource",
            "https://example.org/path/to/resource",
            "http://example.org/path/to/resource/",
            "https://example.org/path/to/resource/",
            "http://example.org/path/to/resource/",
            "https://example.org/path/to/resource/",
            "http://www.example.org/",
            "https://www.example.org/",
            "http://www.example.org/",
            "https://www.example.org/",
            "http://www.example.org/path/to/resource",
            "https://www.example.org/path/to/resource",
            "http://www.example.org/path/to/resource",
            "https://www.example.org/path/to/resource",
            "http://www.example.org/path/to/resource/",
            "https://www.example.org/path/to/resource/",
            "http://www.example.org/path/to/resource/",
            "https://www.example.org/path/to/resource/",
            "http://subdomain.example.org",
            "https://subdomain.example.org",
            "http://subdomain.example.org/",
            "https://subdomain.example.org/",
            "http://subdomain.example.org/",
            "https://subdomain.example.org/",
            "http://subdomain.example.org/path/to/resource",
            "https://subdomain.example.org/path/to/resource",
            "http://subdomain.example.org/path/to/resource",
            "https://subdomain.example.org/path/to/resource",
            "http://subdomain.example.org/path/to/resource/",
            "https://subdomain.example.org/path/to/resource/",
            "http://subdomain.example.org/path/to/resource/",
            "https://subdomain.example.org/path/to/resource/",
            "Some prefix text http://example.org some suffix text",
            "Some prefix text https://example.org some suffix text",
            "Some prefix text http://example.org some suffix text",
            "Some prefix text https://example.org some suffix text",
            "Some prefix text http://example.org/ some suffix text",
            "Some prefix text https://example.org/ some suffix text",
            "Some prefix text http://example.org/ some suffix text",
            "Some prefix text https://example.org/ some suffix text",
            "Some prefix text http://example.org/path/to/resource some suffix text",
            "Some prefix text https://example.org/path/to/resource some suffix text",
            "Some prefix text http://example.org/path/to/resource some suffix text",
            "Some prefix text https://example.org/path/to/resource some suffix text",
            "Some prefix text http://example.org/path/to/resource/ some suffix text",
            "Some prefix text https://example.org/path/to/resource/ some suffix text",
            "Some prefix text http://example.org/path/to/resource/ some suffix text",
            "Some prefix text https://example.org/path/to/resource/ some suffix text",
            "Some prefix text http://www.example.org some suffix text",
            "Some prefix text https://www.example.org some suffix text",
            "Some prefix text http://www.example.org/ some suffix text",
            "Some prefix text https://www.example.org/ some suffix text",
            "Some prefix text http://www.example.org/ some suffix text",
            "Some prefix text https://www.example.org/ some suffix text",
            "Some prefix text http://www.example.org/path/to/resource some suffix text",
            "Some prefix text https://www.example.org/path/to/resource some suffix text",
            "Some prefix text http://www.example.org/path/to/resource some suffix text",
            "Some prefix text https://www.example.org/path/to/resource some suffix text",
            "Some prefix text http://www.example.org/path/to/resource/ some suffix text",
            "Some prefix text https://www.example.org/path/to/resource/ some suffix text",
            "Some prefix text http://www.example.org/path/to/resource/ some suffix text",
            "Some prefix text https://www.example.org/path/to/resource/ some suffix text",
            "Some prefix text http://subdomain.example.org some suffix text",
            "Some prefix text https://subdomain.example.org some suffix text",
            "Some prefix text http://subdomain.example.org/ some suffix text",
            "Some prefix text https://subdomain.example.org/ some suffix text",
            "Some prefix text http://subdomain.example.org/ some suffix text",
            "Some prefix text https://subdomain.example.org/ some suffix text",
            "Some prefix text http://subdomain.example.org/path/to/resource some suffix text",
            "Some prefix text https://subdomain.example.org/path/to/resource some suffix text",
            "Some prefix text http://subdomain.example.org/path/to/resource some suffix text",
            "Some prefix text https://subdomain.example.org/path/to/resource some suffix text",
            "Some prefix text http://subdomain.example.org/path/to/resource/ some suffix text",
            "Some prefix text https://subdomain.example.org/path/to/resource/ some suffix text",
            "Some prefix text http://subdomain.example.org/path/to/resource/ some suffix text",
            "Some prefix text https://subdomain.example.org/path/to/resource/ some suffix text"
        ]
    )
    fun `given a discussion comment is created, when the comment contains invalid contents, then an exception is thrown`(
        comment: String
    ) {
        val topic = ThingId("C123")
        val contributor = ContributorId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } throws InvalidContent()

        shouldThrow<InvalidContent> {
            service.create(CreateDiscussionCommentUseCase.CreateCommand(topic, comment, contributor))
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any()) }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "http://orkg.org",
            "https://orkg.org",
            "http://www.orkg.org",
            "https://www.orkg.org",
            "http://www.orkg.org/",
            "https://www.orkg.org/",
            "http://www.orkg.org/",
            "https://www.orkg.org/",
            "http://www.orkg.org/path/to/resource",
            "https://www.orkg.org/path/to/resource",
            "http://www.orkg.org/path/to/resource",
            "https://www.orkg.org/path/to/resource",
            "http://www.orkg.org/path/to/resource/",
            "https://www.orkg.org/path/to/resource/",
            "http://www.orkg.org/path/to/resource/",
            "https://www.orkg.org/path/to/resource/",
            "http://orkg.org/",
            "https://orkg.org/",
            "http://orkg.org/",
            "https://orkg.org/",
            "http://orkg.org/path/to/resource",
            "https://orkg.org/path/to/resource",
            "http://orkg.org/path/to/resource",
            "https://orkg.org/path/to/resource",
            "http://orkg.org/path/to/resource/",
            "https://orkg.org/path/to/resource/",
            "http://orkg.org/path/to/resource/",
            "https://orkg.org/path/to/resource/",
            "Some prefix text http://orkg.org some suffix text",
            "Some prefix text https://orkg.org some suffix text",
            "Some prefix text http://www.orkg.org some suffix text",
            "Some prefix text https://www.orkg.org some suffix text",
            "Some prefix text http://www.orkg.org/ some suffix text",
            "Some prefix text https://www.orkg.org/ some suffix text",
            "Some prefix text http://www.orkg.org/ some suffix text",
            "Some prefix text https://www.orkg.org/ some suffix text",
            "Some prefix text http://www.orkg.org/path/to/resource some suffix text",
            "Some prefix text https://www.orkg.org/path/to/resource some suffix text",
            "Some prefix text http://www.orkg.org/path/to/resource some suffix text",
            "Some prefix text https://www.orkg.org/path/to/resource some suffix text",
            "Some prefix text http://www.orkg.org/path/to/resource/ some suffix text",
            "Some prefix text https://www.orkg.org/path/to/resource/ some suffix text",
            "Some prefix text http://www.orkg.org/path/to/resource/ some suffix text",
            "Some prefix text https://www.orkg.org/path/to/resource/ some suffix text",
            "Some prefix text http://orkg.org/ some suffix text",
            "Some prefix text https://orkg.org/ some suffix text",
            "Some prefix text http://orkg.org/ some suffix text",
            "Some prefix text https://orkg.org/ some suffix text",
            "Some prefix text http://orkg.org/path/to/resource some suffix text",
            "Some prefix text https://orkg.org/path/to/resource some suffix text",
            "Some prefix text http://orkg.org/path/to/resource some suffix text",
            "Some prefix text https://orkg.org/path/to/resource some suffix text",
            "Some prefix text http://orkg.org/path/to/resource/ some suffix text",
            "Some prefix text https://orkg.org/path/to/resource/ some suffix text",
            "Some prefix text http://orkg.org/path/to/resource/ some suffix text",
            "Some prefix text https://orkg.org/path/to/resource/ some suffix text",
        ]
    )
    fun `given a discussion comment is created, when the comment contains orkg urls, then it returns success`(
        comment: String
    ) {
        val id = DiscussionCommentId(UUID.randomUUID())
        val topic = ThingId("C123")
        val contributor = ContributorId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } returns Optional.of(createClass().copy(id = topic))
        every { repository.nextIdentity() } returns id
        every { repository.save(any()) } returns Unit

        service.create(CreateDiscussionCommentUseCase.CreateCommand(topic, comment, contributor))

        verify(exactly = 1) { thingRepository.findByThingId(any()) }
        verify(exactly = 1) { repository.nextIdentity() }
        verify(exactly = 1) {
            repository.save(
                DiscussionComment(
                    id = id,
                    topic = topic,
                    message = comment,
                    createdBy = contributor,
                    createdAt = staticClock.now()
                )
            )
        }
    }

    @Test
    fun `given a discussion comment is created, when topic is a literal, then an exception is thrown`() {
        val topic = ThingId("C123")
        val comment = "Some comment"
        val contributor = ContributorId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } returns Optional.of(createLiteral().copy(id = topic))

        shouldThrow<TopicNotFound> {
            service.create(CreateDiscussionCommentUseCase.CreateCommand(topic, comment, contributor))
        }

        verify(exactly = 0) { repository.nextIdentity() }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `given a several discussion comments are retrieved, when the topic is valid, then it returns success`() {
        val topic = ThingId("C123")
        val pageable = PageRequest.of(0, 5)

        every { thingRepository.findByThingId(any()) } returns Optional.of(createClass().copy(id = topic))
        every { repository.findAllByTopic(topic, pageable) } returns Page.empty()

        service.findAllByTopic(topic, pageable)

        verify(exactly = 1) { repository.findAllByTopic(topic, pageable) }
    }

    @Test
    fun `given a several discussion comments are retrieved, when the topic is invalid, then an exception is thrown`() {
        val topic = ThingId("C123")
        val pageable = PageRequest.of(0, 5)

        every { thingRepository.findByThingId(any()) } returns Optional.empty()

        shouldThrow<TopicNotFound> {
            service.findAllByTopic(topic, pageable)
        }

        verify(exactly = 0) { repository.findAllByTopic(any(), any()) }
    }

    @Test
    fun `given a several discussion comments are retrieved, when the topic is a literal, then an exception is thrown`() {
        val topic = ThingId("C123")
        val pageable = PageRequest.of(0, 5)

        every { thingRepository.findByThingId(any()) } returns Optional.of(createLiteral().copy(id = topic))

        shouldThrow<TopicNotFound> {
            service.findAllByTopic(topic, pageable)
        }

        verify(exactly = 0) { repository.findAllByTopic(any(), any()) }
    }

    @Test
    fun `given a discussion comment is retrieved, when the topic is valid, then it returns success`() {
        val topic = ThingId("C123")
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = staticClock.now()
        )

        every { thingRepository.findByThingId(any()) } returns Optional.of(createClass().copy(id = topic))
        every { repository.findById(comment.id) } returns Optional.of(comment)

        service.findByTopicAndCommentId(topic, comment.id)

        verify(exactly = 1) { repository.findById(comment.id) }
    }

    @Test
    fun `given a discussion comment is retrieved, when the topic is invalid, then an exception is thrown`() {
        val topic = ThingId("C123")
        val id = DiscussionCommentId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } returns Optional.empty()

        shouldThrow<TopicNotFound> {
            service.findByTopicAndCommentId(topic, id)
        }

        verify(exactly = 0) { repository.findById(any()) }
    }

    @Test
    fun `given a discussion comment is retrieved, when the topic is a literal, then an exception is thrown`() {
        val topic = ThingId("C123")
        val id = DiscussionCommentId(UUID.randomUUID())

        every { thingRepository.findByThingId(any()) } returns Optional.of(createLiteral().copy(id = topic))

        shouldThrow<TopicNotFound> {
            service.findByTopicAndCommentId(topic, id)
        }

        verify(exactly = 0) { repository.findById(any()) }
    }

    @Test
    fun `given a discussion comment is deleted, then it returns success`() {
        val topic = ThingId("C123")
        val userId = ContributorId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = userId,
            createdAt = staticClock.now()
        )
        val user = UserEntity().apply {
            id = userId.value
        }

        every { userService.findById(userId.value) } returns Optional.of(user)
        every { repository.findById(comment.id) } returns Optional.of(comment)
        every { repository.deleteById(comment.id) } returns Unit

        service.delete(userId, topic, comment.id)

        verify(exactly = 1) { repository.deleteById(comment.id) }
    }

    @Test
    fun `given a discussion comment is deleted, when the topic is not found, then it returns success`() {
        val topic = ThingId("C123")
        val userId = ContributorId(UUID.randomUUID())
        val id = DiscussionCommentId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = userId,
            createdAt = staticClock.now()
        )
        val user = UserEntity().apply {
            this.id = userId.value
        }

        every { repository.findById(any()) } returns Optional.of(comment)
        every { userService.findById(userId.value) } returns Optional.of(user)
        every { repository.findById(any()) } returns Optional.empty()

        service.delete(userId, topic, id)

        verify(exactly = 0) { repository.deleteById(id) }
    }

    @Test
    fun `given a discussion comment is deleted, when the user is not found, then an exception is thrown`() {
        val topic = ThingId("C123")
        val userId = ContributorId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = userId,
            createdAt = staticClock.now()
        )

        every { repository.findById(any()) } returns Optional.of(comment)
        every { userService.findById(userId.value) } returns Optional.empty()

        shouldThrow<UserNotFound> {
            service.delete(userId, topic, comment.id)
        }

        verify(exactly = 0) { repository.deleteById(comment.id) }
    }

    @Test
    fun `given a discussion comment is deleted, when the comment is not found, then it returns success`() {
        val topic = ThingId("C123")
        val userId = ContributorId(UUID.randomUUID())
        val id = DiscussionCommentId(UUID.randomUUID())
        val user = UserEntity().apply {
            this.id = userId.value
        }

        every { userService.findById(userId.value) } returns Optional.of(user)
        every { repository.findById(id) } returns Optional.empty()

        service.delete(userId, topic, id)

        verify(exactly = 0) { repository.deleteById(id) }
    }

    @Test
    fun `given a discussion comment is deleted, when the user did not author the comment, then an exception is thrown`() {
        val topic = ThingId("C123")
        val userId = ContributorId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = staticClock.now()
        )
        val user = UserEntity().apply {
            id = userId.value
        }

        every { userService.findById(userId.value) } returns Optional.of(user)
        every { repository.findById(comment.id) } returns Optional.of(comment)
        every { repository.deleteById(comment.id) } returns Unit

        shouldThrow<Unauthorized> {
            service.delete(userId, topic, comment.id)
        }

        verify(exactly = 0) { repository.deleteById(comment.id) }
    }

    @Test
    fun `given a discussion comment is deleted, when the user did not author the comment but is an admin, then it returns success`() {
        val topic = ThingId("C123")
        val userId = ContributorId(UUID.randomUUID())
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = topic,
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = staticClock.now()
        )
        val user = UserEntity().apply {
            id = userId.value
            roles += RoleEntity().apply {
                id = 1
                name = "ROLE_ADMIN"
            }
        }

        every { userService.findById(userId.value) } returns Optional.of(user)
        every { repository.findById(comment.id) } returns Optional.of(comment)
        every { repository.deleteById(comment.id) } returns Unit

        service.delete(userId, topic, comment.id)

        verify(exactly = 1) { repository.deleteById(comment.id) }
    }
}
