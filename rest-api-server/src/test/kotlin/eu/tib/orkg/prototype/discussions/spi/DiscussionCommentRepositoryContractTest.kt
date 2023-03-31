package eu.tib.orkg.prototype.discussions.spi

import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createUser
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionComment
import eu.tib.orkg.prototype.discussions.domain.model.DiscussionCommentId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest

interface DiscussionCommentRepositoryContractTest {
    val repository: DiscussionCommentRepository
    val userRepository: UserRepository

    @Test
    fun `successfully save and load a comment`() {
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = ThingId("C1234"),
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = OffsetDateTime.now()
        )
        userRepository.save(createUser(comment.createdBy.value).toUser())
        repository.save(comment)

        val result = repository.findById(comment.id)
        result.isPresent shouldBe true
        result.get().asClue {
            it.id shouldBe comment.id
            it.topic shouldBe comment.topic
            it.message shouldBe comment.message
            it.createdBy shouldBe comment.createdBy
            it.createdAt shouldBe comment.createdAt
        }
    }

    @Test
    fun `when searching for a comment, and the comment is not in the repository, an empty result is returned from the repository`() {
        val result = repository.findById(DiscussionCommentId(UUID.randomUUID()))
        result.isPresent shouldBe false
    }

    @Test
    fun `given a new new id is requested, it should be different`() {
        val id1 = repository.nextIdentity()
        val id2 = repository.nextIdentity()
        id1 shouldNotBe id2
    }

    @Test
    fun `given a comment is deleted, it should no longer be in the repository`() {
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = ThingId("C1234"),
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = OffsetDateTime.now()
        )
        userRepository.save(createUser(comment.createdBy.value).toUser())
        repository.save(comment)
        repository.findById(comment.id).isPresent shouldBe true

        repository.deleteById(comment.id)

        repository.findById(comment.id).isPresent shouldBe false
    }

    @Test
    fun `finding several comments by topic, it returns the correct result`() {
        val comments = (0..2).map {
            DiscussionComment(
                id = DiscussionCommentId(UUID.randomUUID()),
                topic = ThingId("C1234"),
                message = "Some comment $it",
                createdBy = ContributorId(UUID.randomUUID()),
                createdAt = OffsetDateTime.now().plusHours(it.toLong())
            )
        }.plus(
            DiscussionComment(
                id = DiscussionCommentId(UUID.randomUUID()),
                topic = ThingId("C1235"),
                message = "Some comment",
                createdBy = ContributorId(UUID.randomUUID()),
                createdAt = OffsetDateTime.now()
            )
        )
        comments.forEachIndexed { index, it ->
            userRepository.save(
                createUser(it.createdBy.value).toUser().copy(
                    // Make each email unique to satisfy db constraints
                    email = "user$index@example.org"
                )
            )
            repository.save(it)
        }

        val result = repository.findAllByTopic(ThingId("C1234"), PageRequest.of(0, 5))

        // returns the correct result
        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe 3

        // pages the result correctly
        result.size shouldBe 5
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe 3

        // sorts the results by creation date by default
        result.content.zipWithNext { a, b ->
            a.createdAt shouldBeGreaterThan b.createdAt
        }
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
