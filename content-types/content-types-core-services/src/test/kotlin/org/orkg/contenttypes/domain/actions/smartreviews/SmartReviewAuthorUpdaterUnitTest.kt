package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.actions.AuthorUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateSmartReviewCommand

internal class SmartReviewAuthorUpdaterUnitTest {
    private val authorUpdater: AuthorUpdater = mockk()

    private val smartReviewAuthorUpdater = SmartReviewAuthorUpdater(authorUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(authorUpdater)
    }

    @Test
    fun `Given a smart review update command, it updates the authors`() {
        val command = dummyUpdateSmartReviewCommand()
        val smartReview = createDummySmartReview()
        val state = UpdateSmartReviewState(smartReview = smartReview)

        every { authorUpdater.update(command.contributorId, state.authors, command.smartReviewId) } just runs

        smartReviewAuthorUpdater(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe state.statements
            it.authors shouldBe state.authors
        }

        verify(exactly = 1) { authorUpdater.update(command.contributorId, state.authors, command.smartReviewId) }
    }

    @Test
    fun `Given a smart review update command, when new author list is identical to new author list, it does nothing`() {
        val command = dummyUpdateSmartReviewCommand()
        val smartReview = createDummySmartReview().copy(authors = command.authors!!)
        val state = UpdateSmartReviewState(smartReview = smartReview)

        smartReviewAuthorUpdater(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.statements shouldBe state.statements
            it.authors shouldBe state.authors
        }
    }

    @Test
    fun `Given a smart review update command, when no author list is set, it does nothing`() {
        val command = dummyUpdateSmartReviewCommand().copy(authors = null)
        val state = UpdateSmartReviewState()

        smartReviewAuthorUpdater(command, state).asClue {
            it.smartReview shouldBe state.smartReview
            it.statements shouldBe state.statements
            it.authors shouldBe state.authors
        }
    }
}
