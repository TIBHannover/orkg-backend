package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.publishSmartReviewCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases

internal class SmartReviewVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()

    private val smartReviewVersionHistoryUpdater = SmartReviewVersionHistoryUpdater(statementService)

    @Test
    fun `Given a smart review publish command, it crates a new previous version statement`() {
        val smartReview = createSmartReview()
        val command = publishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val smartReviewVersionId = ThingId("R165")
        val state = PublishSmartReviewState(smartReview, smartReviewVersionId)

        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = smartReview.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = smartReviewVersionId
                )
            )
        } returns StatementId("S1")

        smartReviewVersionHistoryUpdater(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe smartReviewVersionId
        }

        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = smartReview.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = smartReviewVersionId
                )
            )
        }
    }
}
