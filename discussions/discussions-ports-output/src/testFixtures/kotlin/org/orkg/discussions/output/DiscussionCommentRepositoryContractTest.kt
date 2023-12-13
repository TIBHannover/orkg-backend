package org.orkg.discussions.output

import io.kotest.assertions.asClue
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.discussions.domain.DiscussionComment
import org.orkg.discussions.domain.DiscussionCommentId
import org.springframework.data.domain.PageRequest

abstract class DiscussionCommentRepositoryContractTest {
    abstract val repository: DiscussionCommentRepository
    abstract val clock: Clock

    @Test
    fun `successfully save and load a comment`() {
        val comment = DiscussionComment(
            id = DiscussionCommentId(UUID.randomUUID()),
            topic = ThingId("C1234"),
            message = "Some comment",
            createdBy = ContributorId(UUID.randomUUID()),
            createdAt = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS) // There seems to be limitation in Postgres.
        )
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
            createdAt = OffsetDateTime.now(clock)
        )
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
                createdAt = OffsetDateTime.now(clock).plusHours(it.toLong())
            )
        }.plus(
            DiscussionComment(
                id = DiscussionCommentId(UUID.randomUUID()),
                topic = ThingId("C1235"),
                message = "Some comment",
                createdBy = ContributorId(UUID.randomUUID()),
                createdAt = OffsetDateTime.now(clock)
            )
        )
        comments.forEach(repository::save)

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

    abstract fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
