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
import org.orkg.contenttypes.domain.actions.PublishSmartReviewState
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.input.testing.fixtures.publishSmartReviewCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

internal class SmartReviewVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()

    private val smartReviewVersionHistoryUpdater = SmartReviewVersionHistoryUpdater(unsafeStatementUseCases, unsafeResourceUseCases)

    @Test
    fun `Given a smart review publish command, it crates a new previous version statement and updates the previous version smart review class labels`() {
        val smartReview = createSmartReview()
        val command = publishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val smartReviewVersionId = ThingId("R165")
        val state = PublishSmartReviewState(smartReview, smartReviewVersionId)

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = smartReview.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = smartReviewVersionId
                )
            )
        } returns StatementId("S1")
        every { unsafeResourceUseCases.update(any()) } just runs

        smartReviewVersionHistoryUpdater(command, state).asClue {
            it.smartReview shouldBe smartReview
            it.smartReviewVersionId shouldBe smartReviewVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = smartReview.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = smartReviewVersionId
                )
            )
        }
        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = smartReview.versions.published.first().id,
                    contributorId = command.contributorId,
                    classes = setOf(Classes.smartReviewPublished)
                )
            )
        }
    }

    @Test
    fun `Given a smart review publish command, when no previous published version exists, it only crates a new previous version statement`() {
        val smartReview = createSmartReview().let {
            it.copy(versions = it.versions.copy(it.versions.head, emptyList()))
        }
        val command = publishSmartReviewCommand().copy(smartReviewId = smartReview.id)
        val smartReviewVersionId = ThingId("R165")
        val state = PublishSmartReviewState(smartReview, smartReviewVersionId)

        every {
            unsafeStatementUseCases.create(
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
            unsafeStatementUseCases.create(
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
