package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.SmartReviewAlreadyPublished
import org.orkg.contenttypes.domain.SmartReviewNotFound
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishSmartReviewCommand

internal class SmartReviewPublishableValidatorUnitTest {
    private val smartReviewService: SmartReviewUseCases = mockk()

    private val smartReviewPublishableValidator = SmartReviewPublishableValidator(smartReviewService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(smartReviewService)
    }

    @Test
    fun `Given a smart review publish command, when smart review is unpublished, it returns success`() {
        val smartReview = createDummySmartReview()
        val command = dummyPublishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val state = PublishSmartReviewState()

        every { smartReviewService.findById(smartReview.id) } returns Optional.of(smartReview)

        smartReviewPublishableValidator(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe null
        }

        verify(exactly = 1) { smartReviewService.findById(smartReview.id) }
    }

    @Test
    fun `Given a smart review publish command, when smart review is published, it throws an exception`() {
        val smartReview = createDummySmartReview().copy(published = true)
        val command = dummyPublishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val state = PublishSmartReviewState()

        every { smartReviewService.findById(smartReview.id) } returns Optional.of(smartReview)

        assertThrows<SmartReviewAlreadyPublished> { smartReviewPublishableValidator(command, state) }

        verify(exactly = 1) { smartReviewService.findById(smartReview.id) }
    }

    @Test
    fun `Given a smart review publish command, when smart review does not exist, it throws an exception`() {
        val command = dummyPublishSmartReviewCommand()
        val state = PublishSmartReviewState()

        every { smartReviewService.findById(command.smartReviewId) } returns Optional.empty()

        assertThrows<SmartReviewNotFound> { smartReviewPublishableValidator(command, state) }

        verify(exactly = 1) { smartReviewService.findById(command.smartReviewId) }
    }
}
