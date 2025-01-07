package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.DeleteSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewExistenceValidator
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyDeleteSmartReviewSectionCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class SmartReviewSectionExistenceDeleteValidatorUnitTest : MockkBaseTest {
    private val abstractSmartReviewExistenceValidator: AbstractSmartReviewExistenceValidator = mockk()

    private val smartReviewSectionExistenceDeleteValidator =
        SmartReviewSectionExistenceDeleteValidator(abstractSmartReviewExistenceValidator)

    @Test
    fun `Given a smart review section delete command, when checking for smart review existence, it returns success`() {
        val smartReview = createSmartReview()
        val command = dummyDeleteSmartReviewSectionCommand().copy(smartReviewId = smartReview.id)
        val state = DeleteSmartReviewSectionState()
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        every {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id)
        } returns (smartReview to statements)

        smartReviewSectionExistenceDeleteValidator(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe statements
        }

        verify(exactly = 1) {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id)
        }
    }
}
