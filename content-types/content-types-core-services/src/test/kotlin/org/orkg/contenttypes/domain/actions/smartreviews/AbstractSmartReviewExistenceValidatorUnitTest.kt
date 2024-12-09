package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SmartReviewService
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class AbstractSmartReviewExistenceValidatorUnitTest {
    private val smartReviewService: SmartReviewService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val abstractSmartReviewExistenceValidator =
        AbstractSmartReviewExistenceValidator(smartReviewService, resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(smartReviewService, resourceRepository)
    }

    @Test
    fun `Given a smart review id, when checking for smart review existence, it returns success`() {
        val smartReview = createDummySmartReview()
        val root = createResource(id = smartReview.id, classes = setOf(Classes.smartReview))
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(SmartReview.Companion) {
            every { resourceRepository.findById(smartReview.id) } returns Optional.of(root)
            every { smartReviewService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { SmartReview.from(root, root.id, statements) } returns smartReview

            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id).asClue {
                it.first shouldBe smartReview
                it.second shouldBe statements
            }

            verify(exactly = 1) { resourceRepository.findById(smartReview.id) }
            verify(exactly = 1) { smartReviewService.findSubgraph(root) }
            verify(exactly = 1) { SmartReview.from(root, root.id, statements) }
        }
    }

    @Test
    fun `Given a smart review id, when smart review is published, it throws an exception`() {
        val smartReviewId = ThingId("R123")
        val root = createResource(id = smartReviewId, classes = setOf(Classes.smartReviewPublished))

        every { resourceRepository.findById(smartReviewId) } returns Optional.of(root)

        assertThrows<SmartReviewNotModifiable> {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReviewId)
        }

        verify(exactly = 1) { resourceRepository.findById(smartReviewId) }
    }

    @Test
    fun `Given a smart review id command, when smart review is not found, it throws an exception`() {
        val smartReviewId = ThingId("R123")

        every { resourceRepository.findById(smartReviewId) } returns Optional.empty()

        shouldThrow<SmartReviewNotFound> {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReviewId)
        }

        verify(exactly = 1) { resourceRepository.findById(smartReviewId) }
    }
}
