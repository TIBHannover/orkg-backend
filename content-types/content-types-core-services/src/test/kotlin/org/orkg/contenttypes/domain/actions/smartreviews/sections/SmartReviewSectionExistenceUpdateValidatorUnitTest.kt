package org.orkg.contenttypes.domain.actions.smartreviews.sections

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionState
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewExistenceValidator
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewComparisonSectionCommand
import org.orkg.graph.testing.fixtures.createStatement

internal class SmartReviewSectionExistenceUpdateValidatorUnitTest {
    private val abstractSmartReviewExistenceValidator: AbstractSmartReviewExistenceValidator = mockk()

    private val smartReviewSectionExistenceUpdateValidator =
        SmartReviewSectionExistenceUpdateValidator(abstractSmartReviewExistenceValidator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(abstractSmartReviewExistenceValidator)
    }

    @Test
    fun `Given a smart review section update command, when checking for smart review existence, it returns success`() {
        val smartReview = createDummySmartReview()
        val command = dummyUpdateSmartReviewComparisonSectionCommand().copy(smartReviewId = smartReview.id)
        val state = UpdateSmartReviewSectionState()
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        every {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id)
        } returns (smartReview to statements)

        smartReviewSectionExistenceUpdateValidator(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe statements
        }

        verify(exactly = 1) {
            abstractSmartReviewExistenceValidator.findUnpublishedSmartReviewById(smartReview.id)
        }
    }
}
