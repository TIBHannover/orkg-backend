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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

internal class SmartReviewVersionHistoryUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()

    private val smartReviewVersionHistoryUpdater = SmartReviewVersionHistoryUpdater(statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService)
    }

    @Test
    fun `Given a smart review publish command, it crates a new previous version statement`() {
        val smartReview = createDummySmartReview()
        val command = dummyPublishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val smartReviewVersionId = ThingId("R165")
        val state = PublishSmartReviewState(smartReview, smartReviewVersionId)

        every {
            statementService.add(
                userId = command.contributorId,
                subject = smartReview.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = smartReviewVersionId
            )
        } just runs

        smartReviewVersionHistoryUpdater(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe smartReviewVersionId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = smartReview.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = smartReviewVersionId
            )
        }
    }
}
