package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewCommand

internal class SmartReviewSectionsUpdateValidatorUnitTest : MockkBaseTest {
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator = mockk()

    private val smartReviewSectionsUpdateValidator = SmartReviewSectionsUpdateValidator(abstractSmartReviewSectionValidator)

    @Test
    fun `Given a smart review update command, when no smart review sections are defined, it does nothing`() {
        val command = updateSmartReviewCommand().copy(sections = null)
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewState(smartReview = smartReview)

        smartReviewSectionsUpdateValidator(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }
    }

    @Test
    fun `Given a smart review update command, when validating smart review sections, it returns success`() {
        val command = updateSmartReviewCommand()
        val smartReview = createSmartReview()
        val state = UpdateSmartReviewState(smartReview = smartReview)
        val validIds = mutableSetOf(ThingId("R1"), ThingId("P1"), ThingId("R6416"), ThingId("R215648"))

        every { abstractSmartReviewSectionValidator.validate(any(), validIds) } just runs

        smartReviewSectionsUpdateValidator(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe state.statements
            it.authors.size shouldBe 0
        }

        command.sections!!.forEach { section ->
            verify(exactly = 1) { abstractSmartReviewSectionValidator.validate(section, validIds) }
        }
    }
}
