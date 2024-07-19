package org.orkg.contenttypes.domain.actions.smartreviews

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewCommand

class SmartReviewModifiableValidatorUnitTest {
    private val smartReviewModifiableValidator = SmartReviewModifiableValidator()

    @Test
    fun `Given a smart review update command, when smart review is unpublished, it returns success`() {
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = createDummySmartReview())

        smartReviewModifiableValidator(command, state)
    }

    @Test
    fun `Given a smart review update command, when smart review is published, it throws an exception`() {
        val command = dummyUpdateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = createDummySmartReview().copy(published = true))

        assertThrows<SmartReviewNotModifiable> { smartReviewModifiableValidator(command, state) }
    }
}
