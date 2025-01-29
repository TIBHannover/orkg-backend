package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class SmartReviewExistenceValidatorUnitTest : MockkBaseTest {
    private val abstractSmartReviewExistenceValidator: AbstractSmartReviewExistenceValidator = mockk()

    private val smartReviewExistenceValidator =
        SmartReviewExistenceValidator(abstractSmartReviewExistenceValidator)

    @Test
    fun `Given a smart review update command, when checking for smart review existence, it returns success`() {
        val smartReview = createSmartReview()
        val command = updateSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val state = UpdateSmartReviewState()
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        every {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id)
        } returns (smartReview to statements)

        smartReviewExistenceValidator(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe statements
            it.authors shouldBe emptyList()
        }

        verify(exactly = 1) {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id)
        }
    }
}
