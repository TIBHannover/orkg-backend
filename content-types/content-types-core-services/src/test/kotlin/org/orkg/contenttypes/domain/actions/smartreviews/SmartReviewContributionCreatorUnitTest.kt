package org.orkg.contenttypes.domain.actions.smartreviews

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.createSmartReviewCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

internal class SmartReviewContributionCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val smartReviewContributionCreator = SmartReviewContributionCreator(unsafeResourceUseCases, unsafeStatementUseCases)

    @Test
    fun `Given a smart review create command, it crates a new smart review contribution resource`() {
        val command = createSmartReviewCommand()
        val state = CreateSmartReviewState(
            smartReviewId = ThingId("R123")
        )

        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = command.title,
            classes = setOf(Classes.contribution, Classes.contributionSmartReview),
            extractionMethod = command.extractionMethod,
            contributorId = command.contributorId,
            observatoryId = command.observatories.firstOrNull(),
            organizationId = command.organizations.firstOrNull()
        )
        val contributionId = ThingId("R456")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns contributionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.smartReviewId!!,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
        } returns StatementId("S1")

        val result = smartReviewContributionCreator(command, state)

        result.asClue {
            it.smartReviewId shouldBe state.smartReviewId
            it.authors.size shouldBe 0
            it.contributionId shouldBe contributionId
        }

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.smartReviewId!!,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
        }
    }
}
