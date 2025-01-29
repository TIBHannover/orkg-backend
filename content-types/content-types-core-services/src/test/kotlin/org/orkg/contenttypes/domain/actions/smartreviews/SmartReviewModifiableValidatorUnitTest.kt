package org.orkg.contenttypes.domain.actions.smartreviews

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.updateSmartReviewCommand

internal class SmartReviewModifiableValidatorUnitTest {
    private val smartReviewModifiableValidator = SmartReviewModifiableValidator()

    @Test
    fun `Given a smart review update command, when smart review is unpublished, it returns success`() {
        val command = updateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = createSmartReview())

        smartReviewModifiableValidator(command, state)
    }

    @Test
    fun `Given a smart review update command, when smart review is published, it throws an exception`() {
        val command = updateSmartReviewCommand()
        val state = UpdateSmartReviewState(smartReview = createSmartReview().copy(published = true))

        assertThrows<SmartReviewNotModifiable> { smartReviewModifiableValidator(command, state) }
    }
}
