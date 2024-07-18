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
import org.orkg.contenttypes.domain.actions.CreateSmartReviewState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateSmartReviewCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class SmartReviewContributionCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()

    private val smartReviewContributionCreator = SmartReviewContributionCreator(resourceService, statementService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, statementService)
    }

    @Test
    fun `Given a smart review create command, it crates a new smart review contribution resource`() {
        val command = dummyCreateSmartReviewCommand()
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

        every { resourceService.createUnsafe(resourceCreateCommand) } returns contributionId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = state.smartReviewId!!,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        } just runs

        val result = smartReviewContributionCreator(command, state)

        result.asClue {
            it.smartReviewId shouldBe state.smartReviewId
            it.authors.size shouldBe 0
            it.contributionId shouldBe contributionId
        }

        verify(exactly = 1) { resourceService.createUnsafe(resourceCreateCommand) }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = state.smartReviewId!!,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        }
    }
}
